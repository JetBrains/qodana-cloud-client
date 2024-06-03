package org.jetbrains.qodana.cloudclient.v1

import org.jetbrains.qodana.cloudclient.QDCloudResponse

interface QDCloudProjectApiV1 : QDCloudProjectApiV1Versions, QDCloudApiV1Base {
    suspend fun getProjectProperties(): QDCloudResponse<QDCloudSchema.Project>

    suspend fun startUpload(
        request: QDCloudRequestParameters.PublishRequest
    ): QDCloudResponse<QDCloudSchema.StartPublishReportData>

    suspend fun finishUpload(
        reportId: String,
        languages: Map<String, Int>?
    ): QDCloudResponse<QDCloudSchema.FinishPublishReportData>

    // API present in >= 1.3 versions
    interface V3 : QDCloudProjectApiV1Versions.V3 {
    }

    // API present in >= 1.5 versions
    interface V5 : QDCloudProjectApiV1Versions.V5 {
    }
}

/**
 * Add API of a new minor version by declaring new `Vxxx` (like [V3], [V5]) extending the previous version, and a val property like [v3]/[v5]
 */
interface QDCloudProjectApiV1Versions : QDCloudApiV1Versions<QDCloudProjectApiV1> {
    val v3: QDCloudProjectApiV1.V3?

    val v5: QDCloudProjectApiV1.V5?

    interface V3 : QDCloudProjectApiV1Versions {
        override val v3: QDCloudProjectApiV1.V3
    }

    interface V5 : V3 {
        override val v5: QDCloudProjectApiV1.V5
    }
}