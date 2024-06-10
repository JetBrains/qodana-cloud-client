package org.jetbrains.qodana.cloudclient

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.qodana.cloudclient.impl.QDCloudHttpClientImpl
import org.jetbrains.qodana.cloudclient.impl.QDCloudJson
import java.net.http.HttpClient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface QDCloudHttpClient {
    /**
     * Use [request] instead to not care about deserialization
     */
    suspend fun doRequest(host: String, request: QDCloudRequest, token: String?): QDCloudResponse<String>
}

fun QDCloudHttpClient(
    client: HttpClient,
    backoffRetries: Int = 3,
    backoffExponentialBase: Double = 2.0,
    timeout: Duration = 30.seconds,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): QDCloudHttpClient {
    return QDCloudHttpClientImpl(
        client,
        backoffRetries,
        backoffExponentialBase,
        timeout,
        ioDispatcher
    )
}

suspend inline fun <reified T> QDCloudHttpClient.request(
    host: String,
    request: QDCloudRequest,
    token: String?,
): QDCloudResponse<T> {
    return qodanaCloudResponse {
        val content = doRequest(host, request, token).value()
        QDCloudJson.decodeFromString<T>(content)
    }
}