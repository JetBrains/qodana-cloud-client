package org.jetbrains.qodana.cloudclient

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.http.HttpClient

class QDCloudHttpClientTest {
    private val server = MockWebServer()

    @BeforeEach
    fun startServer() {
        server.start()
    }

    @AfterEach
    fun shutdownServer() {
        server.shutdown()
    }

    @Test
    fun `200 response`(): Unit = runBlocking {
        val httpClient = QDCloudHttpClient(HttpClient.newHttpClient())

        @Language("JSON")
        val serverResponse = """
            {
              "name": "Alex",
              "id": "47"
            }
        """.trimIndent()

        val expectedResponse = ServerResponse("Alex", "47")

        val request = QDCloudRequest(
            "user/request",
            QDCloudRequest.GET
        )

        server.enqueue(MockResponse().setResponseCode(200).setBody(serverResponse))

        val response = httpClient.request<ServerResponse>(server.hostPath(), request, null).asSuccess()

        assertThat(response).isEqualTo(expectedResponse)
    }

    @Test
    fun `404 response no retries`(): Unit = runBlocking {
        val httpClient = QDCloudHttpClient(HttpClient.newHttpClient())

        val request = QDCloudRequest(
            "user/request",
            QDCloudRequest.GET
        )

        server.enqueue(MockResponse().setResponseCode(404))

        val response = httpClient.request<ServerResponse>(server.hostPath(), request, null)
                as? QDCloudResponse.Error.ResponseFailure

        assertThat(response?.responseCode).isEqualTo(404)
    }

    @Test
    fun `408 do retries 200`(): Unit = runBlocking {
        val httpClient = QDCloudHttpClient(HttpClient.newHttpClient())

        @Language("JSON")
        val serverResponse = """
            {
              "name": "Alex",
              "id": "47"
            }
        """.trimIndent()

        val expectedResponse = ServerResponse("Alex", "47")

        val request = QDCloudRequest(
            "user/request",
            QDCloudRequest.GET
        )

        server.enqueue(MockResponse().setResponseCode(408))
        server.enqueue(MockResponse().setResponseCode(408))
        server.enqueue(MockResponse().setResponseCode(200).setBody(serverResponse))

        val response = httpClient.request<ServerResponse>(server.hostPath(), request, null).asSuccess()

        assertThat(response).isEqualTo(expectedResponse)
    }

    @Test
    fun `500 do retries 200`(): Unit = runBlocking {
        val httpClient = QDCloudHttpClient(HttpClient.newHttpClient())

        @Language("JSON")
        val serverResponse = """
            {
              "name": "Alex",
              "id": "47"
            }
        """.trimIndent()

        val expectedResponse = ServerResponse("Alex", "47")

        val request = QDCloudRequest(
            "user/request",
            QDCloudRequest.GET
        )

        server.enqueue(MockResponse().setResponseCode(500).setBody(serverResponse))
        server.enqueue(MockResponse().setResponseCode(500).setBody(serverResponse))
        server.enqueue(MockResponse().setResponseCode(200).setBody(serverResponse))

        val response = httpClient.request<ServerResponse>(server.hostPath(), request, null).asSuccess()

        assertThat(response).isEqualTo(expectedResponse)
    }

    @Test
    fun `500 do retries still 500`(): Unit = runBlocking {
        val httpClient = QDCloudHttpClient(HttpClient.newHttpClient())

        val request = QDCloudRequest(
            "user/request",
            QDCloudRequest.GET
        )

        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(500))

        val response = httpClient.request<ServerResponse>(server.hostPath(), request, null)
                as? QDCloudResponse.Error.ResponseFailure

        assertThat(response?.responseCode).isEqualTo(500)
    }

    @Serializable
    private data class ServerResponse(
        @SerialName("name") val name: String?,
        @SerialName("id") val id: String,
    )
}

private fun MockWebServer.hostPath(): String {
    return url("/").toString()
}