package org.jetbrains.qodana.cloudclient

import org.jetbrains.qodana.cloudclient.impl.QDCloudClientImpl
import org.jetbrains.qodana.cloudclient.v1.QDCloudClientV1

/**
 * Main entrypoint to the Qodana Cloud API.
 *
 * [v1] obtains v1 client if the v1 major version is supported by [environment], otherwise returns error
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
