package org.jetbrains.qodana.cloudclient

import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.cloudclient.impl.QDCloudByFrontendEnvironment
import org.jetbrains.qodana.cloudclient.impl.QDCloudCachingEnvironment
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.time.toKotlinDuration

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
            val majorVersion: Int,
            val minorVersion: Int,
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
@JvmOverloads
fun QDCloudEnvironment.requestOn(
    scope: CoroutineScope,
    failureCacheDuration: Duration = Duration.ZERO,
    successCacheDuration: Duration = ChronoUnit.FOREVER.duration,
): QDCloudEnvironment {
    return QDCloudCachingEnvironment(
        this,
        scope,
        failureCacheDuration.toKotlinDuration(),
        successCacheDuration.toKotlinDuration()
    )
}