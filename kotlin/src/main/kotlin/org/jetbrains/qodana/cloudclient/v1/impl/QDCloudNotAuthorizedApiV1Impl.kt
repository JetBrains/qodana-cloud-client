package org.jetbrains.qodana.cloudclient.v1.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.jetbrains.qodana.cloudclient.*
import org.jetbrains.qodana.cloudclient.impl.QDCloudJson
import org.jetbrains.qodana.cloudclient.v1.*
import java.time.Instant

internal class QDCloudNotAuthorizedApiV1Impl(
    override val versionNumber: Int,
    private val host: String,
    private val httpClient: QDCloudHttpClient
) : QDCloudNotAuthorizedApiV1 {
    override val base: QDCloudNotAuthorizedApiV1
        get() = this

    override val v3: QDCloudNotAuthorizedApiV1.V3?
        get() = if (versionNumber >= 3) V3Impl(this) else null

    override val v5: QDCloudNotAuthorizedApiV1.V5?
        get() = if (versionNumber >= 5) V5Impl(v3!!) else null

    override suspend fun getCredentialsFromOAuthCode(
        code: String,
        codeVerifier: String
    ): QDCloudResponse<QDCloudSchema.AuthorizationData> {
        return doRequestForCredentials(
            token = null,
            QDCloudRequest(
                "idea/auth/token/",
                QDCloudRequest.POST(QDCloudJson.encodeToString(mapOf("code" to code)))
            )
        )
    }

    override suspend fun getNewCredentialsFromRefreshCode(
        refreshCode: String
    ): QDCloudResponse<QDCloudSchema.AuthorizationData> {
        return doRequestForCredentials(
            refreshCode,
            QDCloudRequest(
                "idea/auth/refresh/",
                QDCloudRequest.POST()
            )
        )
    }

    override suspend fun getOAuthProviderData(): QDCloudResponse<QDCloudSchema.OAuthProviderData> {
        return request(
            QDCloudRequest(
                "oauth/configurations",
                QDCloudRequest.GET
            )
        )
    }

    override suspend fun doRequest(request: QDCloudRequest): QDCloudResponse<String> {
        return httpClient.doRequest(host, request, token = null)
    }

    private suspend fun doRequestForCredentials(
        token: String?,
        request: QDCloudRequest,
    ): QDCloudResponse<QDCloudSchema.AuthorizationData> {
        return qodanaCloudResponse {
            val data = httpClient.request<RawAuthorizationResponseData>(host, request, token).value()
            QDCloudSchema.AuthorizationData(data.access, data.refresh, Instant.parse(data.expiresAt))
        }
    }

    private class V3Impl(base: QDCloudNotAuthorizedApiV1) : QDCloudNotAuthorizedApiV1.V3, QDCloudNotAuthorizedApiV1Versions by base {
        override val v3: QDCloudNotAuthorizedApiV1.V3
            get() = this
    }

    private class V5Impl(base: QDCloudNotAuthorizedApiV1.V3) : QDCloudNotAuthorizedApiV1.V5, QDCloudNotAuthorizedApiV1Versions.V3 by base {
        override val v5: QDCloudNotAuthorizedApiV1.V5
            get() = this
    }
}

@Serializable
private class RawAuthorizationResponseData(
    @SerialName("access") val access: String,
    @SerialName("refresh") val refresh: String,
    @SerialName("expires_at") val expiresAt: String
)