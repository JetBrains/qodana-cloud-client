@file:Suppress("unused")

package org.jetbrains.qodana.cloudclient.v1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

// namespace
class QDCloudSchema private constructor() {
    @Serializable
    class ReportWithRevision(
        @SerialName("reportId") val reportId: String,
        @SerialName("commit") val commit: String?
    )

    @Serializable
    class UserLicenses(
        @SerialName("missing") val missing: List<License>
    ) {
        @Serializable
        class License(
            @SerialName("id") val id: String
        )
    }

    @Serializable
    class Paginated<T>(
        @SerialName("items") val items: List<T>,
        @SerialName("next") val nextPageOffset: Int?
    )

    @Serializable
    class UserInfo(
        @SerialName("id") val id: String,
        @SerialName("fullName") val fullName: String?,
        @SerialName("username") val username: String?
    )

    @Serializable
    class Organization(
        @SerialName("id") val id: String
    )

    @Serializable
    class Team(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String?,
        @SerialName("projectCount") val projectCount: Int,
        @SerialName("membersCount") val membersCount: Int
    )

    @Serializable
    class Project(
        @SerialName("id") val id: String,
        @SerialName("organizationId") val organizationId: String,
        @SerialName("name") val name: String?
    )

    @Serializable
    class ProjectInTeam(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String?,
        @SerialName("problems") val problems: Problems?,
        @SerialName("branch") val branch: String?,
        @SerialName("lastChecked") val lastChecked: String?,
        @SerialName("baselineCount") val baselineCount: Int?,
        @SerialName("url") val url: String?,
    ) {
        @Serializable
        class Problems(
            @SerialName("total") val total: Int?
        )
    }

    // TODO: Update after resolving of QD-8063
    @Serializable
    class ProjectsByOriginUrl(
        @SerialName("matchingProjects") val matchingProjects: List<MatchingProject>
    )

    @Serializable
    class MatchingProject(
        @SerialName("projectId") val projectId: String,
        @SerialName("projectName") val projectName: String?,
        @SerialName("organizationName") val organizationName: String?,
        @SerialName("teamName") val teamName: String?,
        @SerialName("teamId") val teamId: String?,
        @SerialName("reportInfo") val reportInfo: ReportInfo?
    ) {
        @Serializable
        class ReportInfo(
            @SerialName("problems") val problems: Problems?,
            @SerialName("branch") val branch: String?,
            @SerialName("lastChecked") val lastChecked: String?,
            @SerialName("baselineCount") val baselineCount: Int?,
            @SerialName("url") val url: String?,
        )

        @Serializable
        class Problems(
            @SerialName("total") val total: Int?
        )
    }

    @Serializable
    class Report(
        @SerialName("reportId") val reportId: String
    )

    @Serializable
    class Files(
        @SerialName("files") val files: List<File>
    ) {
        @Serializable
        class File(
            @SerialName("file") val file: String,
            @SerialName("url") val url: String?
        )
    }

    @Serializable
    class ReportData(
        @SerialName("projectId") val projectId: String
    )

    class AuthorizationData(
        val access: String,
        val refresh: String,
        val expiresAt: Instant
    )

    @Serializable
    class OAuthProviderData(
        @SerialName("oauthUrl") val oauthUrl: String,
        @SerialName("providerName") val providerName: String
    )

    @Serializable
    class StartPublishReportData(
        @SerialName("reportId") val reportId: String,
        @SerialName("fileLinks") val fileLinks: Map<String, String>,
        @SerialName("langsRequired") val langsRequired: Boolean,
    )

    @Serializable
    class FinishPublishReportData(
        @SerialName("token") val token: String,
        @SerialName("url") val url: String,
    )
}

// namespace
class QDCloudRequestParameters private constructor() {
    class Paginated(
        private val offset: Int,
        private val limit: Int
    ) {
        fun toMap(): Map<String, String> {
            return mapOf(
                "offset" to offset.toString(),
                "limit" to limit.toString()
            )
        }
    }

    enum class ReportState(private val requestRepresentation: String) {
        UPLOADED("UPLOADED"),
        PROCESSED("PROCESSED"),
        PINNED("PINNED");

        override fun toString(): String = requestRepresentation
    }

    @Serializable
    class PublishRequest(
        @SerialName("type") val type: ReportType,
        @SerialName("files") val files: List<ReportFile>,
        @SerialName("analysisId") val analysisId: String,
        @SerialName("tools") val tools: List<String>?,
        @SerialName("vcs") val vcs: Vcs,
        @SerialName("projectUrl") val projectUrl: String?,
        @SerialName("sharedProjectId") val sharedProjectId: String?,
        @SerialName("publisherVersion") val publisherVersion: String?,
    ) {
        @Serializable
        enum class ReportType {
            @SerialName("idea") IDEA,
            @SerialName("sarif") SARIF;
        }

        @Serializable
        class ReportFile(
            @SerialName("name") val name: String,
            @SerialName("kbSize") val kbSize: Long,
            @SerialName("checksum") val checksum: String,
        )

        @Serializable
        class Vcs(
            @SerialName("commit") val commit: String?,
            @SerialName("branch") val branch: String?,
            @SerialName("author") val author: Author? = null,
            @SerialName("origin") val origin: String? = null,
        )

        @Serializable
        class Author(
            @SerialName("name") val name: String,
            @SerialName("email") val email: String,
        )
    }
}