package org.jetbrains.qodana.cloudclient

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QDCloudClientTest {
    private fun qdCloudMockEnvironment(apis: QDCloudResponse<QDCloudEnvironment.Apis>): QDCloudEnvironment {
        return object : QDCloudEnvironment {
            override suspend fun getApis(): QDCloudResponse<QDCloudEnvironment.Apis> = apis
        }
    }

    private fun apiV(major: Int, minor: Int): QDCloudEnvironment.Apis.Api {
        return QDCloudEnvironment.Apis.Api("host", major, minor)
    }

    @Test
    fun v1_0(): Unit = runBlocking {
        val environment = qdCloudMockEnvironment(
            QDCloudResponse.Success(
                QDCloudEnvironment.Apis(
                    api = listOf(apiV(1, 0)),
                    linters = emptyList()
                )
            )
        )
        val client = QDCloudClient(MockQDCloudHttpClient.empty(), environment)
        val v1 = client.v1().asSuccess()

        assertThat(v1).isNotNull
        v1!!

        val authorized = v1.userApi(::userToken)
        assertThat(authorized.v3).isNull()
        assertThat(authorized.v5).isNull()

        val project = v1.projectApi("token")
        assertThat(project.v3).isNull()
        assertThat(project.v5).isNull()

        val notAuthorized = v1.notAuthorizedApi()
        assertThat(notAuthorized.v3).isNull()
        assertThat(notAuthorized.v5).isNull()
    }

    @Test
    fun v1_3(): Unit = runBlocking {
        val environment = qdCloudMockEnvironment(
            QDCloudResponse.Success(
                QDCloudEnvironment.Apis(
                    api = listOf(apiV(1, 3)),
                    linters = emptyList()
                )
            )
        )
        val client = QDCloudClient(MockQDCloudHttpClient.empty(), environment)
        val v1 = client.v1().asSuccess()

        assertThat(v1).isNotNull
        v1!!

        val authorized = v1.userApi(::userToken)
        assertThat(authorized.v3).isNotNull
        assertThat(authorized.v5).isNull()

        val project = v1.projectApi("token")
        assertThat(project.v3).isNotNull
        assertThat(project.v5).isNull()

        val notAuthorized = v1.notAuthorizedApi()
        assertThat(notAuthorized.v3).isNotNull
        assertThat(notAuthorized.v5).isNull()
    }

    @Test
    fun v1_4(): Unit = runBlocking {
        val environment = qdCloudMockEnvironment(
            QDCloudResponse.Success(
                QDCloudEnvironment.Apis(
                    api = listOf(apiV(1, 4)),
                    linters = emptyList()
                )
            )
        )
        val client = QDCloudClient(MockQDCloudHttpClient.empty(), environment)
        val v1 = client.v1().asSuccess()

        assertThat(v1).isNotNull
        v1!!

        val authorized = v1.userApi(::userToken)
        assertThat(authorized.v3).isNotNull
        assertThat(authorized.v5).isNull()

        val project = v1.projectApi("token")
        assertThat(project.v3).isNotNull
        assertThat(project.v5).isNull()

        val notAuthorized = v1.notAuthorizedApi()
        assertThat(notAuthorized.v3).isNotNull
        assertThat(notAuthorized.v5).isNull()
    }

    @Test
    fun v1_5(): Unit = runBlocking {
        val environment = qdCloudMockEnvironment(
            QDCloudResponse.Success(
                QDCloudEnvironment.Apis(
                    api = listOf(apiV(1, 5)),
                    linters = emptyList()
                )
            )
        )
        val client = QDCloudClient(MockQDCloudHttpClient.empty(), environment)
        val v1 = client.v1().asSuccess()

        assertThat(v1).isNotNull
        v1!!

        val authorized = v1.userApi(::userToken)
        assertThat(authorized.v3).isNotNull
        assertThat(authorized.v5).isNotNull

        val project = v1.projectApi("token")
        assertThat(project.v3).isNotNull
        assertThat(project.v5).isNotNull

        val notAuthorized = v1.notAuthorizedApi()
        assertThat(notAuthorized.v3).isNotNull
        assertThat(notAuthorized.v5).isNotNull
    }

    @Test
    fun `404 from environment`(): Unit = runBlocking {
        val environmentResponse = QDCloudResponse.Error.ResponseFailure(QDCloudException.Error("404", 404))
        val environment = qdCloudMockEnvironment(environmentResponse)
        val client = QDCloudClient(MockQDCloudHttpClient.empty(), environment)
        val v1 = client.v1() as? QDCloudResponse.Error.ResponseFailure

        assertThat(v1).isNotNull
        assertThat(v1?.responseCode)?.isEqualTo(404)
    }

    @Test
    fun `no v1`(): Unit = runBlocking {
        val environment = qdCloudMockEnvironment(
            QDCloudResponse.Success(
                QDCloudEnvironment.Apis(
                    api = listOf(apiV(2, 1)),
                    linters = emptyList()
                )
            )
        )
        val client = QDCloudClient(MockQDCloudHttpClient.empty(), environment)
        val v1 = client.v1() as? QDCloudResponse.Error.ResponseFailure

        assertThat(v1).isNotNull
    }

    private fun userToken(): QDCloudResponse.Success<String> {
        return QDCloudResponse.Success("token")
    }
}