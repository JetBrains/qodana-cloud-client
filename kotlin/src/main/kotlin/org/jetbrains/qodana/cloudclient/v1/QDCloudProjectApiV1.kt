package org.jetbrains.qodana.cloudclient.v1

import org.jetbrains.qodana.cloudclient.QDCloudResponse

/**
 * Add API of a new minor version by declaring new `Vxxx` (like [V3], [V5]) extending the previous version, and a val property like [v3]/[v5]
 */
interface QDCloudProjectApiV1 : QDCloudApiV1 {
    val v3: V3?

    val v5: V5?

    suspend fun getProjectProperties(): QDCloudResponse<QDCloudSchema.Project>

    suspend fun startUpload(
        request: QDCloudRequestParameters.PublishRequest
    ): QDCloudResponse<QDCloudSchema.StartPublishReportData>

    suspend fun finishUpload(
        reportId: String,
        languages: Map<String, Int>?
    ): QDCloudResponse<QDCloudSchema.FinishPublishReportData>

    // API present in >= 1.3 versions
    interface V3 : QDCloudProjectApiV1 {
    }

    // API present in >= 1.5 versions
    interface V5 : V3 {
    }
}