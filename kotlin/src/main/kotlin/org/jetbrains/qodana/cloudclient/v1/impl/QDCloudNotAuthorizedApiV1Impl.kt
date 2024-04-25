package org.jetbrains.qodana.cloudclient.v1.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.jetbrains.qodana.cloudclient.*
import org.jetbrains.qodana.cloudclient.impl.QDCloudJson
import org.jetbrains.qodana.cloudclient.v1.QDCloudNotAuthorizedApiV1
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.cloudclient.v1.request
import java.time.Instant

internal class QDCloudNotAuthorizedApiV1Impl(
    override val minorVersion: Int,
    private val host: String,
    private val httpClient: QDCloudHttpClient
) : QDCloudNotAuthorizedApiV1 {
    override val v3: QDCloudNotAuthorizedApiV1.V3?
        get() = if (minorVersion >= 3) V3Impl(this) else null

    override val v5: QDCloudNotAuthorizedApiV1.V5?
        get() = if (minorVersion >= 5) V5Impl(v3!!) else null

    override suspend fun getCredentialsFromOAuthCode(
        code: String,
        codeVerifier: String
    ): QDCloudResponse<QDCloudSchema.AuthorizationData> {
        return doRequestForCredentials(
            "idea/auth/token/",
            token = null,
            QDCloudRequestMethod.POST(QDCloudJson.encodeToString(mapOf("code" to code)))
        )
    }

    override suspend fun getNewCredentialsFromRefreshCode(
        refreshCode: String
    ): QDCloudResponse<QDCloudSchema.AuthorizationData> {
        return doRequestForCredentials(
            "idea/auth/refresh/",
            refreshCode,
            QDCloudRequestMethod.POST()
        )
    }

    override suspend fun getOAuthProviderData(): QDCloudResponse<QDCloudSchema.OAuthProviderData> {
        return request("oauth/configurations", QDCloudRequestMethod.GET())
    }

    override suspend fun doRequest(
        path: String,
        method: QDCloudRequestMethod,
        headers: Map<String, String>,
    ): QDCloudResponse<String> {
        return httpClient.doRequest(host, path, method, headers, token = null)
    }

    private suspend fun doRequestForCredentials(
        path: String,
        token: String?,
        method: QDCloudRequestMethod,
    ): QDCloudResponse<QDCloudSchema.AuthorizationData> {
        return qodanaCloudResponse {
            val data = httpClient.request<RawAuthorizationResponseData>(host, path, method, emptyMap(), token).value()
            QDCloudSchema.AuthorizationData(data.access, data.refresh, Instant.parse(data.expiresAt))
        }
    }

    private class V3Impl(base: QDCloudNotAuthorizedApiV1) : QDCloudNotAuthorizedApiV1.V3, QDCloudNotAuthorizedApiV1 by base

    private class V5Impl(base: QDCloudNotAuthorizedApiV1.V3) : QDCloudNotAuthorizedApiV1.V5, QDCloudNotAuthorizedApiV1.V3 by base
}

@Serializable
private data class RawAuthorizationResponseData(
    @SerialName("access") val access: String,
    @SerialName("refresh") val refresh: String,
    @SerialName("expires_at") val expiresAt: String
)