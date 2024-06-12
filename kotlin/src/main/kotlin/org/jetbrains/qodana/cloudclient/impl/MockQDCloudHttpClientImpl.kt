package org.jetbrains.qodana.cloudclient.impl

import org.jetbrains.qodana.cloudclient.MockQDCloudHttpClient
import org.jetbrains.qodana.cloudclient.MockQDCloudHttpRequestHandler
import org.jetbrains.qodana.cloudclient.QDCloudRequest
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

internal class MockQDCloudHttpClientImpl : MockQDCloudHttpClient {
    private val handlers: MutableList<MockQDCloudHttpRequestHandler> = CopyOnWriteArrayList()

    private val requestsCounter = AtomicInteger()

    override val requestsCount: Int
        get() = requestsCounter.get()

    override fun respond(handler: MockQDCloudHttpRequestHandler) {
        handlers.add(handler)
    }

    override suspend fun doRequest(host: String, request: QDCloudRequest, token: String?): QDCloudResponse<String> {
        requestsCounter.incrementAndGet()
        val response = handlers.asReversed().firstNotNullOfOrNull { handler ->
            handler.invoke(host, request, token)
        }
        return response ?: throw MockQDCloudHttpClient.NotSupportedException()
    }
}