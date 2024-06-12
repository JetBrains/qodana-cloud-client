package org.jetbrains.qodana.cloudclient.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.qodana.cloudclient.*

private const val INVALID_VERSION_FORMAT = "Invalid version format"

internal class QDCloudByFrontendEnvironment(
    private val frontendUrl: String,
    private val httpClient: QDCloudHttpClient
) : QDCloudEnvironment {

    override suspend fun getApis(): QDCloudResponse<QDCloudEnvironment.Apis> {
        return qodanaCloudResponse {
            val urls = httpClient.request<BackendUrls>(
                frontendUrl,
                QDCloudRequest(
                    "api/versions",
                    QDCloudRequest.GET
                ),
                token = null
            ).value()

            val api = urls.api.versions.map { it.asApi() }
            val linters = urls.linters?.versions?.map { it.asApi() } ?: emptyList()

            QDCloudEnvironment.Apis(api, linters)
        }
    }

    private fun BackendUrls.VersionUrl.asApi(): QDCloudEnvironment.Apis.Api {
        val host = url
        val (major, minor) = try {
            version.split(".").map { it.toInt() }
        } catch (e : NumberFormatException) {
            throw QDCloudException.Error(INVALID_VERSION_FORMAT, responseCode = null, cause = e)
        } catch (e : IndexOutOfBoundsException) {
            throw QDCloudException.Error(INVALID_VERSION_FORMAT, responseCode = null, cause = e)
        }
        return QDCloudEnvironment.Apis.Api(host, major, minor)
    }

    @Serializable
    private class BackendUrls(
        @SerialName("api") val api: VersionsHolder,
        @SerialName("linters") val linters: VersionsHolder?
    ) {
        @Serializable
        class VersionsHolder(
            @SerialName("versions") val versions: List<VersionUrl>
        )

        @Serializable
        class VersionUrl(
            @SerialName("version") val version: String,
            @SerialName("url") val url: String
        )
    }
}