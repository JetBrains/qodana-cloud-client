package org.jetbrains.qodana.cloudclient.v1

import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudRequest
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

/**
 * Represents available versions and relations between them (see [QDCloudUserApiV1Versions] as an example)
 *
 * Relations between versions are extracted to this separate interface to avoid inheritance between apis themselves:
 * so that code `v5.v3.callV3()` is valid, but `v5.callV3()` is not: to do a call to v3 api you need to get v3 version
 */
interface QDCloudApiV1Versions<out T> where T : QDCloudApiV1Versions<T>, T : QDCloudApiV1Base {
    // exact minor version on the BE
    val versionNumber: Int

    // base api, always available
    val base: T
}

interface QDCloudApiV1Base {
    /**
     * Use [request] instead to not care about deserialization
     */
    suspend fun doRequest(request: QDCloudRequest): QDCloudResponse<String>
}

suspend inline fun <reified T> QDCloudApiV1Base.request(request: QDCloudRequest): QDCloudResponse<T> {
    return qodanaCloudResponse {
        val content = doRequest(request).value()
        QDCloudJson.decodeFromString<T>(content)
    }
}