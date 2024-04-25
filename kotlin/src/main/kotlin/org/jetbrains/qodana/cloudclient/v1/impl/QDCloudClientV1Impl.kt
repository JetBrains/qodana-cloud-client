package org.jetbrains.qodana.cloudclient.v1.impl

import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.v1.*

internal class QDCloudClientV1Impl(
    private val host: String,
    private val minorVersion: Int,
    private val httpClient: QDCloudHttpClient,
) : QDCloudClientV1 {
    override fun userApi(
        userTokenProvider: QDCloudUserTokenProvider
    ): QDCloudUserApiV1 {
        return QDCloudUserApiV1Impl(minorVersion, host, httpClient, userTokenProvider)
    }

    override fun projectApi(
        projectToken: QDCloudProjectToken
    ): QDCloudProjectApiV1 {
        return QDCloudProjectApiV1Impl(minorVersion, host, httpClient, projectToken)
    }

    override fun notAuthorizedApi(): QDCloudNotAuthorizedApiV1 {
        return QDCloudNotAuthorizedApiV1Impl(minorVersion, host, httpClient)
    }
}