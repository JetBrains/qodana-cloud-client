package org.jetbrains.qodana.cloudclient.v1

import org.jetbrains.qodana.cloudclient.QDCloudResponse
import java.time.Instant

/**
 * Add API of a new minor version by declaring new `Vxxx` (like [V3], [V5]) extending the ***V1Versions.Vxxx
 */
interface QDCloudUserApiV1 : QDCloudUserApiV1Versions, QDCloudApiV1Base {
    suspend fun getUserLicenses(): QDCloudResponse<QDCloudSchema.UserLicenses>

    suspend fun getUserInfo(): QDCloudResponse<QDCloudSchema.UserInfo>

    suspend fun getOrganizations(): QDCloudResponse<List<QDCloudSchema.Organization>>

    suspend fun getTeams(
        organizationId: String,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.Team>>

    suspend fun getProjectsOfTeam(
        teamId: String,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.ProjectInTeam>>

    suspend fun getProjectByOriginUrl(
        originUrl: String
    ): QDCloudResponse<QDCloudSchema.ProjectsByOriginUrl>

    suspend fun getProjectProperties(
        projectId: String
    ): QDCloudResponse<QDCloudSchema.Project>

    suspend fun getReportsTimeline(
        projectId: String,
        states: List<QDCloudRequestParameters.ReportState>,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.Report>>

    /**
     * to > from
     */
    suspend fun getReportsWithRevisionsForPeriod(
        projectId: String,
        from: Instant?,
        to: Instant?,
        paginatedRequestParameters: QDCloudRequestParameters.Paginated
    ): QDCloudResponse<QDCloudSchema.Paginated<QDCloudSchema.ReportWithRevision>>

    suspend fun getReportFiles(
        reportId: String,
        filenames: List<String>
    ): QDCloudResponse<QDCloudSchema.Files>

    suspend fun getReportData(
        reportId: String
    ): QDCloudResponse<QDCloudSchema.ReportData>

    suspend fun generateProjectToken(projectId: String): QDCloudResponse<QDCloudSchema.ProjectToken>

    suspend fun getProjectToken(projectId: String): QDCloudResponse<QDCloudSchema.ProjectToken>

    // API present in >= 1.3 versions
    interface V3 : QDCloudUserApiV1Versions.V3

    // API present in >= 1.3 versions
    interface V5 : QDCloudUserApiV1Versions.V5
}

/**
 * Add API of a new minor version by declaring new `Vxxx` (like [V3], [V5]) extending the previous version, and a val property like [v3]/[v5]
 *
 * See [QDCloudApiV1Versions]
 */
interface QDCloudUserApiV1Versions : QDCloudApiV1Versions<QDCloudUserApiV1> {
    val v3: QDCloudUserApiV1.V3?

    val v5: QDCloudUserApiV1.V5?

    interface V3 : QDCloudUserApiV1Versions {
        override val v3: QDCloudUserApiV1.V3
    }

    interface V5 : V3 {
        override val v5: QDCloudUserApiV1.V5
    }
}