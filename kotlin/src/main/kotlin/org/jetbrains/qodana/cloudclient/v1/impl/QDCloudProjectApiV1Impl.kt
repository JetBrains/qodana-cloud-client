package org.jetbrains.qodana.cloudclient.v1.impl

import kotlinx.serialization.encodeToString
import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudRequestMethod
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.impl.QDCloudJson
import org.jetbrains.qodana.cloudclient.v1.*

internal class QDCloudProjectApiV1Impl(
    override val minorVersion: Int,
    private val host: String,
    private val httpClient: QDCloudHttpClient,
    private val token: QDCloudProjectToken,
) : QDCloudProjectApiV1 {
    override val v3: QDCloudProjectApiV1.V3?
        get() = if (minorVersion >= 3) V3Impl(this) else null

    override val v5: QDCloudProjectApiV1.V5?
        get() = if (minorVersion >= 5) V5Impl(v3!!) else null

    override suspend fun getProjectProperties(): QDCloudResponse<QDCloudSchema.Project> {
        return request("projects", QDCloudRequestMethod.GET())
    }

    override suspend fun startUpload(
        request: QDCloudRequestParameters.PublishRequest
    ): QDCloudResponse<QDCloudSchema.StartPublishReportData> {
        return request("reports", QDCloudRequestMethod.POST(QDCloudJson.encodeToString(request)))
    }

    override suspend fun finishUpload(
        reportId: String,
        languages: Map<String, Int>?
    ): QDCloudResponse<QDCloudSchema.FinishPublishReportData> {
        val body = languages?.let {
            QDCloudJson.encodeToString(mapOf("languages" to languages))
        } ?: ""
        return request(
            "reports/$reportId/finish",
            QDCloudRequestMethod.POST(body)
        )
    }

    @Deprecated("Use `request` instead", replaceWith = ReplaceWith("request"), level = DeprecationLevel.WARNING)
    override suspend fun doRequest(
        path: String,
        method: QDCloudRequestMethod,
        headers: Map<String, String>,
    ): QDCloudResponse<String> {
        return httpClient.doRequest(host, path, method, headers, token)
    }

    private class V3Impl(base: QDCloudProjectApiV1) : QDCloudProjectApiV1.V3, QDCloudProjectApiV1 by base

    private class V5Impl(base: QDCloudProjectApiV1.V3) : QDCloudProjectApiV1.V5, QDCloudProjectApiV1.V3 by base
}