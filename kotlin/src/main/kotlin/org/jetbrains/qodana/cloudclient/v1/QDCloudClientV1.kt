package org.jetbrains.qodana.cloudclient.v1

import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudRequestMethod
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.impl.QDCloudJson
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.impl.QDCloudClientV1Impl

typealias QDCloudUserToken = String
typealias QDCloudProjectToken = String
typealias QDCloudUserTokenProvider = suspend () -> QDCloudResponse<QDCloudUserToken>

/**
 * Entrypoint to Qodana Cloud V1 api, separated by auth types
 */
interface QDCloudClientV1 {
    fun userApi(userTokenProvider: QDCloudUserTokenProvider): QDCloudUserApiV1

    fun projectApi(projectToken: QDCloudProjectToken): QDCloudProjectApiV1

    fun notAuthorizedApi(): QDCloudNotAuthorizedApiV1
}

fun QDCloudClientV1(
    host: String,
    minorVersion: Int,
    httpClient: QDCloudHttpClient,
): QDCloudClientV1 {
    return QDCloudClientV1Impl(host, minorVersion, httpClient)
}

interface QDCloudApiV1 {
    // exact minor version on the BE
    val minorVersion: Int

    @Deprecated("Use `request` instead", replaceWith = ReplaceWith("request"), level = DeprecationLevel.WARNING)
    suspend fun doRequest(
        path: String,
        method: QDCloudRequestMethod,
        headers: Map<String, String> = emptyMap(),
    ): QDCloudResponse<String>
}

suspend inline fun <reified T> QDCloudApiV1.request(
    path: String,
    method: QDCloudRequestMethod,
    headers: Map<String, String> = emptyMap(),
): QDCloudResponse<T> {
    return qodanaCloudResponse {
        val content = doRequest(path, method, headers).value()
        QDCloudJson.decodeFromString<T>(content)
    }
}