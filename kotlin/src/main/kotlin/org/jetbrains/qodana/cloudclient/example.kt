package org.jetbrains.qodana.cloudclient

import kotlinx.coroutines.GlobalScope
import java.net.http.HttpClient

@Suppress("unused")
private suspend fun example() {
    val client = createClient()

    processUserName(client)
    processProjectName(client)
}

private fun createClient(): QDCloudClient {
    val httpClient = QDCloudHttpClient(HttpClient.newHttpClient())
    val environment = QDCloudEnvironment("https://qodana.cloud", httpClient).requestOn(GlobalScope)
    return QDCloudClient(httpClient, environment)
}

private suspend fun processUserName(client: QDCloudClient) {
    val userName = qodanaCloudResponse {
        val userApi = client.v1().value().userApi(::userToken)
        val v5 = userApi.v5
        val v3 = userApi.v3
        when {
            v5 != null -> {
                // v5-specific call v5.xxx().value()
                // here you can access APIs: (v1, v1.3, v1.5) like this: v5.v3.xxx(), v5.versions.base.xxx()
                "user name from V5"
            }
            v3 != null -> {
                // v3-specific call v3.xxx().value()
                // here you can access APIs: (v1, v1.3) like this: v3.versions.base.xxx()
                "user name from V3"
            }
            else -> {
                userApi.getUserInfo().value().fullName
            }
        }
    }
    when(userName) {
        is QDCloudResponse.Success -> {
            val name = userName.value
            println("Username is $name")
        }
        is QDCloudResponse.Error.Offline -> {
            println("Failed to obtain username, qodana cloud is not available")
        }
        is QDCloudResponse.Error.ResponseFailure -> {
            println("Failed to obtain username, code: ${userName.responseCode}, message: ${userName.errorMessage}")
        }
    }
}

private suspend fun processProjectName(client: QDCloudClient) {
    val projectName = qodanaCloudResponse {
        val projectApi = client.v1().value().projectApi("token")
        val v5 = projectApi.v5
        val v3 = projectApi.v3
        when {
            v5 != null -> {
                // v5-specific call v5.xxx().value()
                // here you can access APIs: (v1, v1.3, v1.5) like this: v5.v3.xxx(), v5.base.xxx()
                "project name from V5"
            }
            v3 != null -> {
                // v3-specific call v3.xxx().value()
                // here you can access APIs: (v1, v1.3) like this: v3.base.xxx()
                "project name from V3"
            }
            else -> {
                projectApi.getProjectProperties().value().name
            }
        }
    }
    when(projectName) {
        is QDCloudResponse.Success -> {
            val name = projectName.value
            println("Project name is $name")
        }
        is QDCloudResponse.Error.Offline -> {
            println("Failed to obtain project name, qodana cloud is not available")
        }
        is QDCloudResponse.Error.ResponseFailure -> {
            println("Failed to obtain project name, code: ${projectName.responseCode}, message: ${projectName.errorMessage}")
        }
    }
}

private fun userToken(): QDCloudResponse.Success<String> {
    return QDCloudResponse.Success("user-token")
}