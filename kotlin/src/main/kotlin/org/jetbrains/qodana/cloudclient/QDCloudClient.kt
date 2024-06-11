package org.jetbrains.qodana.cloudclient

import org.jetbrains.qodana.cloudclient.impl.QDCloudClientImpl
import org.jetbrains.qodana.cloudclient.v1.QDCloudClientV1

/**
 * Main entrypoint to the Qodana Cloud API.
 *
 * [v1] obtains v1 client if the v1 major version is supported by [environment], otherwise returns error
 *
 * Usage example:
 *
 * ```
 * suspend fun main() {
 *     val client = createClient()
 *
 *     processUserName(client)
 *     processProjectName(client)
 * }
 *
 * private fun createClient(): QDCloudClient {
 *     val httpClient = QDCloudHttpClient(HttpClient.newHttpClient())
 *     val environment = QDCloudEnvironment("https://qodana.cloud", httpClient).requestOn(someCoroutineScope)
 *     return QDCloudClient(httpClient, environment)
 * }
 *
 * private suspend fun processUserName(client: QDCloudClient) {
 *     val userName = qodanaCloudResponse {
 *         val userApi = client.v1().value().userApi(::userToken)
 *         val v5 = userApi.v5
 *         val v3 = userApi.v3
 *         when {
 *             v5 != null -> {
 *                 // v5-specific call v5.xxx().value()
 *                 // here you can access APIs: (v1.3, v1) like this: v5.v3.xxx(), v5.base.xxx()
 *                 "user name from V5"
 *             }
 *             v3 != null -> {
 *                 // v3-specific call v3.xxx().value()
 *                 // here you can access APIs: (v1) like this: v3.base.xxx()
 *                 "user name from V3"
 *             }
 *             else -> {
 *                 userApi.getUserInfo().value().fullName
 *             }
 *         }
 *     }
 *     when(userName) {
 *         is QDCloudResponse.Success -> {
 *             val name = userName.value
 *             println("Username is $name")
 *         }
 *         is QDCloudResponse.Error.Offline -> {
 *             println("Failed to obtain username, qodana cloud is not available")
 *         }
 *         is QDCloudResponse.Error.ResponseFailure -> {
 *             println("Failed to obtain username, code: ${userName.responseCode}, message: ${userName.errorMessage}")
 *         }
 *     }
 * }
 *
 * private suspend fun processProjectName(client: QDCloudClient) {
 *     val projectName = qodanaCloudResponse {
 *         val projectApi = client.v1().value().projectApi("token")
 *         val v5 = projectApi.v5
 *         val v3 = projectApi.v3
 *         when {
 *             v5 != null -> {
 *                 // v5-specific call v5.xxx().value()
 *                 // here you can access APIs: (v1.3, v1) like this: v5.v3.xxx(), v5.base.xxx()
 *                 "project name from V5"
 *             }
 *             v3 != null -> {
 *                 // v3-specific call v3.xxx().value()
 *                 // here you can access APIs: (v1) like this: v3.base.xxx()
 *                 "project name from V3"
 *             }
 *             else -> {
 *                 projectApi.getProjectProperties().value().name
 *             }
 *         }
 *     }
 *     when(projectName) {
 *         is QDCloudResponse.Success -> {
 *             val name = projectName.value
 *             println("Project name is $name")
 *         }
 *         is QDCloudResponse.Error.Offline -> {
 *             println("Failed to obtain project name, qodana cloud is not available")
 *         }
 *         is QDCloudResponse.Error.ResponseFailure -> {
 *             println("Failed to obtain project name, code: ${projectName.responseCode}, message: ${projectName.errorMessage}")
 *         }
 *     }
 * }
 *
 * private fun userToken(): QDCloudResponse.Success<String> {
 *     return QDCloudResponse.Success("user-token")
 * }
 * ```
 */
interface QDCloudClient {
    val httpClient: QDCloudHttpClient

    val environment: QDCloudEnvironment

    suspend fun v1(): QDCloudResponse<QDCloudClientV1>
}

fun QDCloudClient(
    httpClient: QDCloudHttpClient,
    environment: QDCloudEnvironment
): QDCloudClient {
    return QDCloudClientImpl(httpClient, environment)
}
