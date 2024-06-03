package org.jetbrains.qodana.cloudclient.v1

import org.jetbrains.qodana.cloudclient.QDCloudResponse

interface QDCloudNotAuthorizedApiV1 : QDCloudNotAuthorizedApiV1Versions, QDCloudApiV1Base {
    suspend fun getCredentialsFromOAuthCode(
        code: String,
        codeVerifier: String
    ): QDCloudResponse<QDCloudSchema.AuthorizationData>

    suspend fun getNewCredentialsFromRefreshCode(
        refreshCode: String
    ): QDCloudResponse<QDCloudSchema.AuthorizationData>

    suspend fun getOAuthProviderData(): QDCloudResponse<QDCloudSchema.OAuthProviderData>

    // API present in >= 1.3 versions
    interface V3 : QDCloudNotAuthorizedApiV1Versions.V3 {
    }

    // API present in >= 1.5 versions
    interface V5 : QDCloudNotAuthorizedApiV1Versions.V5 {
    }
}

/**
 * Add API of a new minor version by declaring new `Vxxx` (like [V3], [V5]) extending the previous version, and a val property like [v3]/[v5]
 */
interface QDCloudNotAuthorizedApiV1Versions : QDCloudApiV1Versions<QDCloudNotAuthorizedApiV1> {
    val v3: QDCloudNotAuthorizedApiV1.V3?

    val v5: QDCloudNotAuthorizedApiV1.V5?

    interface V3 : QDCloudNotAuthorizedApiV1Versions {
        override val v3: QDCloudNotAuthorizedApiV1.V3
    }

    interface V5 : V3 {
        override val v5: QDCloudNotAuthorizedApiV1.V5
    }
}