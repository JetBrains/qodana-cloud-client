package org.jetbrains.qodana.cloudclient.v1.impl

import kotlinx.serialization.encodeToString
import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudRequest
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.impl.QDCloudJson
import org.jetbrains.qodana.cloudclient.v1.*

internal class QDCloudProjectApiV1Impl(
    override val versionNumber: Int,
    private val host: String,
    private val httpClient: QDCloudHttpClient,
    private val token: QDCloudProjectToken,
) : QDCloudProjectApiV1 {
    override val base: QDCloudProjectApiV1
        get() = this

    override val v3: QDCloudProjectApiV1.V3?
        get() = if (versionNumber >= 3) V3Impl(this) else null

    override val v5: QDCloudProjectApiV1.V5?
        get() = if (versionNumber >= 5) V5Impl(v3!!) else null

    override suspend fun getProjectProperties(): QDCloudResponse<QDCloudSchema.Project> {
        return request(
            QDCloudRequest(
                "projects",
                QDCloudRequest.GET
            )
        )
    }

    override suspend fun startUpload(
        request: QDCloudRequestParameters.PublishRequest
    ): QDCloudResponse<QDCloudSchema.StartPublishReportData> {
        return request(
            QDCloudRequest(
                "reports",
                QDCloudRequest.POST(QDCloudJson.encodeToString(request))
            )
        )
    }

    override suspend fun finishUpload(
        reportId: String,
        languages: Map<String, Int>?
    ): QDCloudResponse<QDCloudSchema.FinishPublishReportData> {
        val body = languages?.let {
            QDCloudJson.encodeToString(mapOf("languages" to languages))
        } ?: ""
        return request(
            QDCloudRequest(
                "reports/$reportId/finish",
                QDCloudRequest.POST(body)
            )
        )
    }

    override suspend fun doRequest(request: QDCloudRequest): QDCloudResponse<String> {
        return httpClient.doRequest(host, request, token)
    }

    private class V3Impl(base: QDCloudProjectApiV1) : QDCloudProjectApiV1.V3, QDCloudProjectApiV1Versions by base {
        override val v3: QDCloudProjectApiV1.V3
            get() = this
    }

    private class V5Impl(base: QDCloudProjectApiV1.V3) : QDCloudProjectApiV1.V5, QDCloudProjectApiV1Versions.V3 by base {
        override val v5: QDCloudProjectApiV1.V5
            get() = this
    }
}