package org.jetbrains.qodana.cloudclient.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.qodana.cloudclient.*

internal class QDCloudByFrontendEnvironment(
    private val frontendUrl: String,
    private val httpClient: QDCloudHttpClient
) : QDCloudEnvironment {

    override suspend fun getApis(): QDCloudResponse<QDCloudEnvironment.Apis> {
        return qodanaCloudResponse {
            val urls = httpClient.request<BackendUrls>(
                frontendUrl,
                "api/versions",
                QDCloudRequestMethod.GET(),
                emptyMap(),
                token = null
            ).value()

            val api = urls.api.versions.map { it.asApi() }
            val linters = urls.linters.versions.map { it.asApi() }

            QDCloudEnvironment.Apis(api, linters)
        }
    }

    private fun BackendUrls.VersionUrl.asApi(): QDCloudEnvironment.Apis.Api {
        val host = url
        val (major, minor) = version.split(".").map { it.toInt() }
        return QDCloudEnvironment.Apis.Api(host, major, minor)
    }

    @Serializable
    private data class BackendUrls(
        @SerialName("api") val api: VersionsHolder,
        @SerialName("linters") val linters: VersionsHolder
    ) {
        @Serializable
        data class VersionsHolder(
            @SerialName("versions") val versions: List<VersionUrl>
        )

        @Serializable
        data class VersionUrl(
            @SerialName("version") val version: String,
            @SerialName("url") val url: String
        )
    }
}