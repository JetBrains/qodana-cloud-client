package org.jetbrains.qodana.cloudclient

import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class QDCloudEnvironmentTest {
    private val frontend = "qodana.cloud"

    private val httpClient = MockQDCloudHttpClient.empty()
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("EnvironmentTest")
    )

    private fun environmentByFrontend(): QDCloudEnvironment {
        return QDCloudEnvironment(
            frontendUrl = frontend,
            httpClient = httpClient
        )
    }

    @Test
    fun `valid single api version`(): Unit = runBlocking {
        @Language("JSON")
        val frontendVersionsResponse = """
            {
              "api": {
                "versions": [
                  {
                    "version": "1.1",
                    "url": "backend-url"
                  }
                ]
              }
            }
        """.trimIndent()

        val expectedApis = QDCloudEnvironment.Apis(
            api = listOf(
                QDCloudEnvironment.Apis.Api("backend-url", 1, 1)
            ),
            linters = emptyList()
        )

        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                frontendVersionsResponse
            }
        }

        val environment = environmentByFrontend()

        val apis = environment.getApis().asSuccess()

        assertThat(apis).isEqualTo(expectedApis)
    }

    @Test
    fun `valid single api and linter version`(): Unit = runBlocking {
        @Language("JSON")
        val frontendVersionsResponse = """
            {
              "api": {
                "versions": [
                  {
                    "version": "1.1",
                    "url": "backend-url"
                  }
                ]
              },
              "linters": {
                "versions": [
                  {
                    "version": "2.10",
                    "url": "linters-backend-url"
                  }
                ]
              }
            }
        """.trimIndent()

        val expectedApis = QDCloudEnvironment.Apis(
            api = listOf(
                QDCloudEnvironment.Apis.Api("backend-url", 1, 1),
            ),
            linters = listOf(
                QDCloudEnvironment.Apis.Api("linters-backend-url", 2, 10),
            )
        )

        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                frontendVersionsResponse
            }
        }

        val environment = environmentByFrontend()

        val apis = environment.getApis().asSuccess()

        assertThat(apis).isEqualTo(expectedApis)
    }

    @Test
    fun `valid multiple api versions`(): Unit = runBlocking {
        @Language("JSON")
        val frontendVersionsResponse = """
            {
              "api": {
                "versions": [
                  {
                    "version": "1.1",
                    "url": "backend-url"
                  },
                  {
                    "version": "2.5",
                    "url": "backend-url-2"
                  }
                ]
              }
            }
        """.trimIndent()

        val expectedApis = QDCloudEnvironment.Apis(
            api = listOf(
                QDCloudEnvironment.Apis.Api("backend-url", 1, 1),
                QDCloudEnvironment.Apis.Api("backend-url-2", 2, 5)
            ),
            linters = emptyList()
        )

        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                frontendVersionsResponse
            }
        }

        val environment = environmentByFrontend()

        val apis = environment.getApis().asSuccess()

        assertThat(apis).isEqualTo(expectedApis)
    }

    @Test
    fun `invalid api version`(): Unit = runBlocking {
        @Language("JSON")
        val frontendVersionsResponse = """
            {
              "api": {
                "versions": [
                  {
                    "version": "1.invalid",
                    "url": "backend-url"
                  }
                ]
              }
            }
        """.trimIndent()

        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                frontendVersionsResponse
            }
        }

        val environment = environmentByFrontend()

        val apis = environment.getApis() as? QDCloudResponse.Error.ResponseFailure

        assertThat(apis).isNotNull
    }

    @Test
    fun `404 from server`(): Unit = runBlocking {
        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                throw QDCloudException.Error("404", 404)
            }
        }

        val environment = environmentByFrontend()
        val apis = environment.getApis() as? QDCloudResponse.Error.ResponseFailure

        assertThat(apis).isNotNull
        assertThat(apis?.responseCode)?.isEqualTo(404)
    }

    @Test
    fun `caching environment on scope two requests one server call`(): Unit = runBlocking {
        @Language("JSON")
        val frontendVersionsResponse = """
            {
              "api": {
                "versions": [
                  {
                    "version": "1.1",
                    "url": "backend-url"
                  }
                ]
              }
            }
        """.trimIndent()

        val expectedApis = QDCloudEnvironment.Apis(
            api = listOf(
                QDCloudEnvironment.Apis.Api("backend-url", 1, 1)
            ),
            linters = emptyList()
        )

        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                frontendVersionsResponse
            }
        }

        val environment = environmentByFrontend().requestOn(scope)

        val apis1 = environment.getApis().asSuccess()
        assertThat(apis1).isEqualTo(expectedApis)

        val apis2 = environment.getApis().asSuccess()
        assertThat(apis2).isEqualTo(expectedApis)

        assertThat(httpClient.requestsCount).isEqualTo(1)
    }

    @Test
    fun `caching environment on scope two concurrent requests one server call`(): Unit = runBlocking {
        @Language("JSON")
        val frontendVersionsResponse = """
            {
              "api": {
                "versions": [
                  {
                    "version": "1.1",
                    "url": "backend-url"
                  }
                ]
              }
            }
        """.trimIndent()

        val expectedApis = QDCloudEnvironment.Apis(
            api = listOf(
                QDCloudEnvironment.Apis.Api("backend-url", 1, 1)
            ),
            linters = emptyList()
        )

        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                delay(2.seconds)
                frontendVersionsResponse
            }
        }

        val environment = environmentByFrontend().requestOn(scope)

        coroutineScope {
            launch {
                val apis1 = environment.getApis().asSuccess()
                assertThat(apis1).isEqualTo(expectedApis)
            }
            launch {
                val apis2 = environment.getApis().asSuccess()
                assertThat(apis2).isEqualTo(expectedApis)
            }
        }

        assertThat(httpClient.requestsCount).isEqualTo(1)
    }

    @Test
    fun `caching environment error then success`(): Unit = runBlocking {
        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                throw QDCloudException.Error("404", 404)
            }
        }

        val environment = environmentByFrontend().requestOn(scope)

        val apis1 = environment.getApis() as? QDCloudResponse.Error.ResponseFailure

        assertThat(apis1).isNotNull
        assertThat(apis1?.responseCode)?.isEqualTo(404)

        @Language("JSON")
        val frontendVersionsResponse = """
            {
              "api": {
                "versions": [
                  {
                    "version": "1.1",
                    "url": "backend-url"
                  }
                ]
              }
            }
        """.trimIndent()

        val expectedApis = QDCloudEnvironment.Apis(
            api = listOf(
                QDCloudEnvironment.Apis.Api("backend-url", 1, 1)
            ),
            linters = emptyList()
        )

        httpClient.respond(frontend, "api/versions") {
            qodanaCloudResponse {
                frontendVersionsResponse
            }
        }

        val apis2 = environment.getApis().asSuccess()
        assertThat(apis2).isEqualTo(expectedApis)

        assertThat(httpClient.requestsCount).isEqualTo(2)
    }
}