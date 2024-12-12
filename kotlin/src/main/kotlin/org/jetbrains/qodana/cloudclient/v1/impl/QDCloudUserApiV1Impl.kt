package org.jetbrains.qodana.cloudclient.v1.impl

import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudRequest
import org.jetbrains.qodana.cloudclient.QDCloudResponse
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
        return request(
            QDCloudRequest(
                "users/me/licenses",
                QDCloudRequest.GET
            )
        )
    }

    override suspend fun getUserInfo(): QDCloudResponse<QDCloudSchema.UserInfo> {
        return request(
            QDCloudRequest(
                "users/me",
                QDCloudRequest.GET
            )
        )
    }

    override suspend fun getOrganizations(): QDCloudResponse<List<QDCloudSchema.Organization>> {
        return request(
            QDCloudRequest(
                "organizations",
                QDCloudRequest.GET
            )
        )
    }

    override suspend fun getTeams(
        organizationId: String,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.Team>> {
        return request(
            QDCloudRequest(
                "organizations/$organizationId/teams",
                QDCloudRequest.GET,
                parameters = paginatedRequestParameters.toMap()
            )
        )
    }

    override suspend fun getProjectsOfTeam(
        teamId: String,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.ProjectInTeam>> {
        return request(
            QDCloudRequest(
                "teams/${teamId}/projects",
                QDCloudRequest.GET,
                parameters = paginatedRequestParameters.toMap()
            )
        )
    }

    override suspend fun getProjectByOriginUrl(originUrl: String): QDCloudResponse<QDCloudSchema.ProjectsByOriginUrl> {
        return request(
            QDCloudRequest(
                "projects/search",
                QDCloudRequest.GET,
                parameters = mapOf("originUrl" to originUrl)
            )
        )
    }

    override suspend fun getProjectProperties(projectId: String): QDCloudResponse<QDCloudSchema.Project> {
        return request(
            QDCloudRequest(
                "projects/$projectId",
                QDCloudRequest.GET
            )
        )
    }

    override suspend fun getReportsTimeline(
        projectId: String,
        states: List<QDCloudRequestParameters.ReportState>,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.Report>> {
        return request(
            QDCloudRequest(
                "projects/$projectId/timeline",
                QDCloudRequest.GET,
                parameters = paginatedRequestParameters.toMap() + mapOf("states" to states.joinToString(","))
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

        val fromToParameters = buildMap {
            if (from != null) {
                put("from", from.toString())
            }
            if (to != null) {
                put("to", to.toString())
            }
        }
        return request(
            QDCloudRequest(
                "projects/$projectId/revisions",
                QDCloudRequest.GET,
                parameters = paginatedRequestParameters.toMap() + fromToParameters
            )
        )
    }

    override suspend fun getReportFiles(
        reportId: String,
        filenames: List<String>
    ): QDCloudResponse<QDCloudSchema.Files> {
        return request(
            QDCloudRequest(
                "reports/$reportId/files",
                QDCloudRequest.GET,
                parameters = mapOf("paths" to filenames.joinToString(","))
            )
        )
    }

    override suspend fun getReportData(reportId: String): QDCloudResponse<QDCloudSchema.ReportData> {
        return request(
            QDCloudRequest(
                "reports/$reportId",
                QDCloudRequest.GET
            )
        )
    }

    override suspend fun generateProjectToken(projectId: String): QDCloudResponse<QDCloudSchema.ProjectToken> {
        return request(
            QDCloudRequest(
                "projects/$projectId/tokens",
                QDCloudRequest.POST()
            )
        )
    }

    override suspend fun getProjectToken(projectId: String): QDCloudResponse<QDCloudSchema.ProjectToken> {
        return request(
            QDCloudRequest(
                "projects/$projectId/token",
                QDCloudRequest.GET
            )
        )
    }

    override suspend fun doRequest(request: QDCloudRequest): QDCloudResponse<String> {
        return qodanaCloudResponse {
            val token = tokenProvider.invoke().value()
            httpClient.doRequest(host, request, token).value()
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

