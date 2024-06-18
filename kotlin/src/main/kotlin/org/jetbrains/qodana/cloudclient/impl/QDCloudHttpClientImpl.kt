package org.jetbrains.qodana.cloudclient.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.cloudclient.*
import java.io.IOException
import java.net.SocketException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration
import kotlin.io.path.Path
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

internal class QDCloudHttpClientImpl(
    private val client: HttpClient,
    private val backoffRetries: Int,
    private val backoffExponentialBase: Double,
    private val timeout: Duration,
    private val ioDispatcher: CoroutineDispatcher,
) : QDCloudHttpClient {
    override suspend fun doRequest(host: String, request: QDCloudRequest, token: String?): QDCloudResponse<String> {
        val httpRequest = buildRequestToQodanaCloud(host, request, token)
        return sendRequestToQodanaCloud(httpRequest)
    }

    private fun buildRequestToQodanaCloud(
        host: String,
        request: QDCloudRequest,
        authToken: String? = null,
    ): HttpRequest {
        val url = validateAndBuildRequestUrl(host, request.path, request.parameters)

        val requestBuilder = HttpRequest.newBuilder(url)
            .header("Content-Type", "application/json")

        if (request.headers.isNotEmpty()) {
            requestBuilder.headers(*(request.headers.flatMap { listOf(it.key, it.value) }).toTypedArray())
        }
        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer $authToken")
        }
        requestBuilder.timeout(timeout)
        when(val requestType = request.type) {
            QDCloudRequest.GET -> {
                requestBuilder.GET()
            }
            is QDCloudRequest.POST -> {
                requestBuilder.POST(requestType.body.asRequestBody())
            }
            QDCloudRequest.DELETE -> {
                requestBuilder.DELETE()
            }
            is QDCloudRequest.PUT -> {
                requestBuilder.PUT(requestType.body.asRequestBody())
            }
            is QDCloudRequest.Other -> {
                requestBuilder.method(requestType.name, requestType.body.asRequestBody())
            }
        }
        return requestBuilder.build()
    }

    private suspend fun sendRequestToQodanaCloud(request: HttpRequest): QDCloudResponse<String> {
        return qodanaCloudResponse {
            val response = flow {
                try {
                    val response = runInterruptible(ioDispatcher) {
                        client.send(request, HttpResponse.BodyHandlers.ofString())
                    }
                    val responseCode = response.statusCode()
                    if (response.statusCode() !in (200 until 300)) {
                        throw QDCloudException.Error(response.body(), responseCode)
                    }
                    emit(response)
                } catch (e: SocketException) {
                    throw QDCloudException.Offline(e)
                } catch (e : HttpTimeoutException) {
                    throw QDCloudException.Offline(e)
                } catch (e: IOException) {
                    throw QDCloudException.Offline(e)
                }
            }.retryWhen { e, attempt ->
                val is400ErrorNotRetry = e is QDCloudException.Error &&
                        e.responseCode in (400 until 500) &&
                        e.responseCode != 408
                val isErrorRetry = e is QDCloudException && !is400ErrorNotRetry
                if (isErrorRetry && attempt + 1 < backoffRetries) {
                    delay(backoffExponentialBase.pow(attempt.toInt()).seconds)
                    true
                } else {
                    false
                }
            }.first()

            response.body()
        }
    }
}

private fun String?.asRequestBody(): HttpRequest.BodyPublisher {
    return this?.let { BodyPublishers.ofString(it) } ?: BodyPublishers.noBody()
}

// there is no adequate API in JDK to work with request parameters
// You can't use URI(...), because the encoding on your side is still needed,
// but then URI performs some additional encoding over your encoding
private fun validateAndBuildRequestUrl(
    host: String,
    path: String,
    parameters: Map<String, String>
): URI {
    fun parametersPart(): String {
        if (parameters.isEmpty()) return ""

        val encoded = parameters.toList().joinToString("&") { (key, value) ->
            val encodedKey = URLEncoder.encode(key, "UTF-8")
            val encodedValue = URLEncoder.encode(value, "UTF-8")
            "$encodedKey=$encodedValue"
        }
        return "?$encoded"
    }

    val pathToSanitize = Path(path)
    require(!pathToSanitize.isAbsolute) {
        "Qodana Cloud API request path must be relative"
    }
    require(!pathToSanitize.contains(Path(".."))) {
        "Qodana Cloud API request path can not contain parent directory references"
    }
    val correctedHost = if (host.endsWith("/")) host else "$host/"
    return URI("$correctedHost$path${parametersPart()}")
}