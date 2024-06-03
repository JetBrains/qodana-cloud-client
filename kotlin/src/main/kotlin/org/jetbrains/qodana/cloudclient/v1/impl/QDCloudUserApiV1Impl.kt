package org.jetbrains.qodana.cloudclient.v1.impl

import kotlinx.serialization.encodeToString
import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudRequestMethod
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.impl.QDCloudJson
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.*
import java.time.Instant

internal class QDCloudUserApiV1Impl(
    override val versionNumber: Int,
    private val host: String,
    private val httpClient: QDCloudHttpClient,
    private val tokenProvider: QDCloudUserTokenProvider
) : QDCloudUserApiV1 {
    override val base: QDCloudUserApiV1
        get() = this

    override val v3: QDCloudUserApiV1.V3?
        get() = if (versionNumber >= 3) V3Impl(this) else null

    override val v5: QDCloudUserApiV1.V5?
        get() = if (versionNumber >= 5) V5Impl(v3!!) else null

    override suspend fun getUserLicenses(): QDCloudResponse<QDCloudSchema.UserLicenses> {
        return request("users/me/licenses", QDCloudRequestMethod.GET())
    }

    override suspend fun getUserInfo(): QDCloudResponse<QDCloudSchema.UserInfo> {
        return request("users/me", QDCloudRequestMethod.GET())
    }

    override suspend fun getOrganizations(): QDCloudResponse<List<QDCloudSchema.Organization>> {
        return request("organizations", QDCloudRequestMethod.GET())
    }

    override suspend fun getTeams(
        organizationId: String,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.Team>> {
        return request("organizations/$organizationId/teams", QDCloudRequestMethod.GET(paginatedRequestParameters.toMap()))
    }

    override suspend fun getProjectsOfTeam(
        teamId: String,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.ProjectInTeam>> {
        return request("teams/${teamId}/projects", QDCloudRequestMethod.GET(paginatedRequestParameters.toMap()))
    }

    override suspend fun getProjectByOriginUrl(originUrl: String): QDCloudResponse<QDCloudSchema.ProjectsByOriginUrl> {
        return request("projects/search", QDCloudRequestMethod.GET(mapOf("originUrl" to originUrl)))
    }

    override suspend fun getProjectProperties(projectId: String): QDCloudResponse<QDCloudSchema.Project> {
        return request("projects/$projectId", QDCloudRequestMethod.GET())
    }

    override suspend fun getReportsTimeline(
        projectId: String,
        states: List<QDCloudRequestParameters.ReportState>,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.Report>> {
        return request(
            "projects/$projectId/timeline",
            QDCloudRequestMethod.GET(
                paginatedRequestParameters.toMap() +
                        mapOf("states" to states.joinToString(","))
            )
        )
    }

    override suspend fun getReportsWithRevisionsForPeriod(
        projectId: String,
        from: Instant?,
        to: Instant?,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.ReportWithRevision>> {
        require((from ?: Instant.MIN) <= ((to ?: Instant.MAX))) { "to must be >= from" }
        return request(
            "projects/$projectId/revisions",
            QDCloudRequestMethod.GET(
                paginatedRequestParameters.toMap() +
                        buildMap {
                            if (from != null) {
                                put("from", from.toString())
                            }
                            if (to != null) {
                                put("to", to.toString())
                            }
                        }
            )
        )
    }

    override suspend fun getReportFiles(
        reportId: String,
        filenames: List<String>
    ): QDCloudResponse<List<QDCloudSchema.Files>> {
        return request(
            "reports/$reportId/files",
            QDCloudRequestMethod.GET(mapOf("paths" to filenames.joinToString(",")))
        )
    }

    override suspend fun getReportData(reportId: String): QDCloudResponse<QDCloudSchema.ReportData> {
        return request("reports/$reportId", QDCloudRequestMethod.GET())
    }

    override suspend fun createProjectInTeam(
        teamId: String,
        name: String
    ): QDCloudResponse<QDCloudSchema.Project> {
        return request(
            "teams/$teamId/projects/",
            QDCloudRequestMethod.POST(QDCloudJson.encodeToString(mapOf("name" to name)))
        )
    }

    @Deprecated("Use `request` instead", replaceWith = ReplaceWith("request"), level = DeprecationLevel.WARNING)
    override suspend fun doRequest(
        path: String,
        method: QDCloudRequestMethod,
        headers: Map<String, String>,
    ): QDCloudResponse<String> {
        return qodanaCloudResponse {
            val token = tokenProvider.invoke().value()
            httpClient.doRequest(host, path, method, headers, token).value()
        }
    }

    private class V3Impl(
        val baseImpl: QDCloudUserApiV1
    ) : QDCloudUserApiV1.V3, QDCloudUserApiV1Versions by baseImpl {
        override val v3: QDCloudUserApiV1.V3
            get() = this
    }

    private class V5Impl(
        val baseImpl: QDCloudUserApiV1.V3
    ) : QDCloudUserApiV1.V5, QDCloudUserApiV1Versions.V3 by baseImpl {
        override val v5: QDCloudUserApiV1.V5
            get() = this
    }
}

