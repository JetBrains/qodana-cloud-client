package org.jetbrains.qodana.cloudclient

import org.jetbrains.annotations.TestOnly
import org.jetbrains.qodana.cloudclient.impl.MockQDCloudHttpClientImpl

typealias MockQDCloudHttpRequestHandler =
        suspend (host: String, request: QDCloudRequest, token: String?) -> QDCloudResponse<String>?

/**
 * For use in unit tests, use [respond] to set response on some request
 */
@TestOnly
interface MockQDCloudHttpClient : QDCloudHttpClient {
    companion object {
        fun empty(): MockQDCloudHttpClient = MockQDCloudHttpClientImpl()
    }

    val requestsCount: Int

    fun respond(handler: MockQDCloudHttpRequestHandler)

    class NotSupportedException : Exception()
}

fun MockQDCloudHttpClient.respond(
    host: String,
    path: String,
    handler: suspend (request: QDCloudRequest) -> QDCloudResponse<String>?
) {
    val fullHandler: MockQDCloudHttpRequestHandler = { requestHost: String, request: QDCloudRequest, token: String? ->
        if (requestHost != host || request.path != path) null else handler.invoke(request)
    }
    respond(fullHandler)
}