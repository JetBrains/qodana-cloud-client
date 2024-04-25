package org.jetbrains.qodana.cloudclient

import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.cloudclient.impl.QDCloudByFrontendEnvironment
import org.jetbrains.qodana.cloudclient.impl.QDCloudCachingEnvironment
import kotlin.time.Duration

/**
 * Provides data about available backend API versions
 * (calls <frontend>/api/versions)
 */
interface QDCloudEnvironment {
    suspend fun getApis(): QDCloudResponse<Apis>

    data class Apis(
        val api: List<Api>,
        val linters: List<Api>
    ) {
        data class Api(
            val host: String,
            val minorVersion: Int,
            val majorVersion: Int,
        )
    }
}

fun QDCloudEnvironment(
    frontendUrl: String,
    httpClient: QDCloudHttpClient
): QDCloudEnvironment {
    return QDCloudByFrontendEnvironment(frontendUrl, httpClient)
}

/**
 * Limits all requests for version to one simultaneously running coroutine on [scope]
 *
 * Response is cached with [failureCacheDuration] and [successCacheDuration]
 *
 * TODO – move this on [QDCloudHttpClient]?
 */
fun QDCloudEnvironment.requestOn(
    scope: CoroutineScope,
    failureCacheDuration: Duration = Duration.ZERO,
    successCacheDuration: Duration = Duration.INFINITE,
): QDCloudEnvironment {
    return QDCloudCachingEnvironment(this, scope, failureCacheDuration, successCacheDuration)
}