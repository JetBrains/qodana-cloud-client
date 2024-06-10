package org.jetbrains.qodana.cloudclient

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

fun MockWebServer.hostPath(): String {
    return url("/").toString()
}

fun MockWebServer.respond(path: String, responseProvider: (RecordedRequest) -> MockResponse?) {
    respond { request ->
        when(path) {
            request.path -> responseProvider.invoke(request)
            else -> null
        }
    }
}

fun MockWebServer.respond(responseProvider: (RecordedRequest) -> MockResponse?) {
    val currentDispatcher = dispatcher
    val newDispatcher = TestMockWebServerDispatcher { request ->
        if (currentDispatcher is TestMockWebServerDispatcher) {
            responseProvider.invoke(request) ?: currentDispatcher.responseProvider.invoke(request)
        } else {
            responseProvider.invoke(request)
        }
    }
    dispatcher = newDispatcher
}

private class TestMockWebServerDispatcher(val responseProvider: (RecordedRequest) -> MockResponse?) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        return responseProvider.invoke(request) ?: error("Not supported request")
    }
}