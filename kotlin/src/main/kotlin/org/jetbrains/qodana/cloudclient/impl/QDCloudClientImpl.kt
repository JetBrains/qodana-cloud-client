package org.jetbrains.qodana.cloudclient.impl

import org.jetbrains.qodana.cloudclient.*
import org.jetbrains.qodana.cloudclient.v1.QDCloudClientV1

private const val V1_NOT_SUPPORTED_ERROR = "Qodana Cloud does not support V1 API"

internal class QDCloudClientImpl(
    override val httpClient: QDCloudHttpClient,
    override val environment: QDCloudEnvironment
) : QDCloudClient {
    override suspend fun v1(): QDCloudResponse<QDCloudClientV1> {
        return qodanaCloudResponse {
            val apis = environment.getApis().value()
            val v1Major = apis.api
                .find { it.majorVersion == 1 }
                ?: throw QDCloudException.Error(V1_NOT_SUPPORTED_ERROR, responseCode = null, cause = null)

            QDCloudClientV1(v1Major.host, v1Major.minorVersion, httpClient)
        }
    }
}