package org.jetbrains.qodana.cloudclient

@DslMarker
private annotation class QDCloudResponseBlockDsl

/**
 * Qodana Cloud response is always wrapped in [QDCloudResponse] to enforce the client to respect the error case
 *
 * Use .value() inside [qodanaCloudResponse] block to safely unwrap the value
 */
sealed interface QDCloudResponse<out T> {
    class Success<out T>(val value: T) : QDCloudResponse<T>

    sealed interface Error : QDCloudResponse<Nothing> {
        val exception: QDCloudException

        class Offline(override val exception: QDCloudException.Offline) : Error

        class ResponseFailure(override val exception: QDCloudException.Error) : Error {
            val errorMessage: String
                get() = exception.errorMessage

            val responseCode: Int?
                get() = exception.responseCode
        }
    }
}

sealed class QDCloudException : Exception() {
    class Offline(override val cause: Throwable? = null) : QDCloudException()

    class Error(val errorMessage: String, val responseCode: Int?, override val cause: Throwable? = null) : QDCloudException()
}

/**
 * Inside [qodanaCloudResponse] it's possible to safely unwrap the QDCloudResponse using .value(), or safely throw [QDCloudException]
 */
@QDCloudResponseBlockDsl
suspend fun <T> qodanaCloudResponse(action: suspend QDCloudResponseBlock.() -> T): QDCloudResponse<T> {
    return try {
        val value = with(RESPONSE_BLOCK) {
            action()
        }
        QDCloudResponse.Success(value)
    }
    catch (e : QDCloudException) {
        when(e) {
            is QDCloudException.Error -> QDCloudResponse.Error.ResponseFailure(e)
            is QDCloudException.Offline -> QDCloudResponse.Error.Offline(e)
        }
    }
}

fun <T> QDCloudResponse<T>.asSuccess(): T? {
    return when(this) {
        is QDCloudResponse.Success -> value
        is QDCloudResponse.Error -> null
    }
}

private val RESPONSE_BLOCK = QDCloudResponseBlock()

class QDCloudResponseBlock internal constructor() {
    fun <T> QDCloudResponse<T>.value(): T {
        return when(this) {
            is QDCloudResponse.Success -> value
            is QDCloudResponse.Error -> throw exception
        }
    }
}

