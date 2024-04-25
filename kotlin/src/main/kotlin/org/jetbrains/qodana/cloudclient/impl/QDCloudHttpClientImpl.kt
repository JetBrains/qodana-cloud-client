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
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import kotlin.io.path.Path
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

internal class QDCloudHttpClientImpl(
    private val client: HttpClient,
    private val backoffRetries: Int,
    private val backoffExponentialBase: Double,
    private val timeout: Duration,
    private val ioDispatcher: CoroutineDispatcher,
) : QDCloudHttpClient {
    @Deprecated("Use `request` instead", replaceWith = ReplaceWith("request"), level = DeprecationLevel.WARNING)
    override suspend fun doRequest(
        host: String,
        path: String,
        method: QDCloudRequestMethod,
        headers: Map<String, String>,
        token: String?,
    ): QDCloudResponse<String> {
        val request = buildRequestToQodanaCloud(host, path, method, headers, token)
        return sendRequestToQodanaCloud(request)
    }

    private fun buildRequestToQodanaCloud(
        host: String,
        path: String,
        method: QDCloudRequestMethod,
        headers: Map<String, String> = emptyMap(),
        authToken: String? = null,
    ): HttpRequest {
        val pathToSanitize = Path(path)
        require(!pathToSanitize.isAbsolute) {
            "Qodana Cloud API request path must be relative"
        }
        require(!pathToSanitize.contains(Path(".."))) {
            "Qodana Cloud API request path can not contain parent directory references"
        }

        val url = URI(host).resolve(path).withParameters(method.parameters)

        val requestBuilder = HttpRequest.newBuilder(url)
            .header("Content-Type", "application/json")

        if (headers.isNotEmpty()) {
            requestBuilder.headers(*(headers.flatMap { listOf(it.key, it.value) }).toTypedArray())
        }
        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer $authToken")
        }
        requestBuilder.timeout(timeout.toJavaDuration())
        when(method) {
            is QDCloudRequestMethod.GET -> {
                requestBuilder.GET()
            }
            is QDCloudRequestMethod.POST -> {
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(method.body))
            }
            is QDCloudRequestMethod.DELETE -> {
                requestBuilder.DELETE()
            }
            is QDCloudRequestMethod.PUT -> {
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(method.body))
            }
            is QDCloudRequestMethod.Other -> {
                val body = HttpRequest.BodyPublishers.ofString(method.body)
                requestBuilder.method(method.name, body)
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
                    throw QDCloudException.Error("Request timeout", null, cause = e)
                } catch (e: IOException) {
                    throw QDCloudException.Error(e.message ?: "IO error occurred", null, cause = e)
                }
            }.retryWhen { e, attempt ->
                if (e is QDCloudException && attempt < backoffRetries) {
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

private fun URI.withParameters(parameters: Map<String, String>): URI {
    var uri = this
    parameters.forEach { (t, u) ->
        uri = uri.withParameter(t, u)
    }
    return uri
}

private fun URI.withParameter(paramName: String, paramValue: String): URI {
    // Encode the parameter name and value to handle special characters
    val encodedParamName = URLEncoder.encode(paramName, "UTF-8")
    val encodedParamValue = URLEncoder.encode(paramValue, "UTF-8")

    // Construct the query, test if original query is not null and prepend an '&' if it's not empty
    val newQuery = (query?.let { "$it&" } ?: "") + "$encodedParamName=$encodedParamValue"

    // Construct the new URI with the updated query
    return URI(
        scheme,
        authority,
        path,
        newQuery,  // use the new query string
        fragment
    )
}