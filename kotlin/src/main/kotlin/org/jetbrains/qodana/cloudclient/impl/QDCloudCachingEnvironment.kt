@file:OptIn(ExperimentalTime::class)

package org.jetbrains.qodana.cloudclient.impl

import kotlinx.coroutines.*
import org.jetbrains.qodana.cloudclient.QDCloudEnvironment
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

internal class QDCloudCachingEnvironment(
    private val environment: QDCloudEnvironment,
    private val scope: CoroutineScope,
    private val failureCacheDuration: Duration,
    private val successCacheDuration: Duration,
) : QDCloudEnvironment {
    private val cachedRequest: AtomicReference<Deferred<QDCloudApisResponseWithTimestamp>?> =
        AtomicReference(null)

    override suspend fun getApis(): QDCloudResponse<QDCloudEnvironment.Apis> {
        val request = cachedRequest.get()
        if (request != null) {
            val requestWasRunning = !request.isCompleted
            val responseWithTimestamp = request.await()

            val response = responseWithTimestamp.response
            val cachedResponseDuration = currentTimestamp() - responseWithTimestamp.timestamp

            if (requestWasRunning ||
                response is QDCloudResponse.Success && cachedResponseDuration <= successCacheDuration ||
                response is QDCloudResponse.Error && cachedResponseDuration <= failureCacheDuration
            ) {
                return response
            }
            cachedRequest.compareAndSet(request, null)
        }

        val newRequest = lazy {
            scope.async(start = CoroutineStart.LAZY) {
                try {
                    val newResponse = environment.getApis()
                    return@async QDCloudApisResponseWithTimestamp(newResponse, currentTimestamp())
                }
                catch (e : Throwable) {
                    cachedRequest.set(null)
                    throw e
                }
            }
        }

        val currentRequest = cachedRequest.updateAndGet { current ->
            return@updateAndGet current ?: newRequest.value
        }
        requireNotNull(currentRequest) { "Updated request can not be null" }

        if (newRequest.isInitialized() && currentRequest !== newRequest.value) {
            newRequest.value.cancelAndJoin()
        }

        return currentRequest.await().response
    }
}

private class QDCloudApisResponseWithTimestamp(
    val response: QDCloudResponse<QDCloudEnvironment.Apis>,
    val timestamp: TimeSource.Monotonic.ValueTimeMark
)

private fun currentTimestamp(): TimeSource.Monotonic.ValueTimeMark {
    return TimeSource.Monotonic.markNow()
}