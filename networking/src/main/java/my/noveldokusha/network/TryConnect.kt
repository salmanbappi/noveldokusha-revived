package my.noveldokusha.network

import my.noveldokusha.core.Response
import my.noveldokusha.core.flatMapError
import my.noveldokusha.core.flatten
import my.noveldokusha.core.tryAsResponse
import java.net.SocketTimeoutException

import kotlinx.coroutines.delay
import java.io.IOException

suspend fun <T> tryFlatConnect(
    extraErrorInfo: String = "",
    maxRetries: Int = 2,
    call: suspend () -> Response<T>
): Response<T> {
    var attempt = 0
    var currentDelay = 500L
    while (true) {
        val result = tryAsResponse { call() }.flatten().specifyNetworkErrors(extraErrorInfo)
        if (result is Response.Success || attempt >= maxRetries) {
            return result
        }
        val isTransient = result is Response.Error && (result.exception is SocketTimeoutException || result.exception is IOException)
        if (!isTransient) {
            return result
        }
        attempt++
        delay(currentDelay)
        currentDelay *= 2
    }
}

suspend fun <T> tryConnect(
    extraErrorInfo: String = "",
    maxRetries: Int = 2,
    call: suspend () -> T
): Response<T> {
    var attempt = 0
    var currentDelay = 500L
    while (true) {
        val result = tryAsResponse { call() }.specifyNetworkErrors(extraErrorInfo)
        if (result is Response.Success || attempt >= maxRetries) {
            return result
        }
        val isTransient = result is Response.Error && (result.exception is SocketTimeoutException || result.exception is IOException)
        if (!isTransient) {
            return result
        }
        attempt++
        delay(currentDelay)
        currentDelay *= 2
    }
}


private suspend fun <T> Response<T>.specifyNetworkErrors(extraErrorInfo: String = "") =
    flatMapError {
        when (it.exception) {
            is SocketTimeoutException -> {
                val error = listOf(
                    "Timeout error.",
                    "",
                    "Info:",
                    extraErrorInfo.ifBlank { "No info" },
                    "",
                    "Message:",
                    it.exception.message
                ).joinToString("\n")

                Response.Error(error, it.exception)
            }
            else -> {
                val error = listOf(
                    "Unknown error.",
                    "",
                    "Info:",
                    extraErrorInfo.ifBlank { "No Info" },
                    "",
                    "Message:",
                    it.exception.message,
                    "",
                    "Stacktrace:",
                    it.exception.stackTraceToString()
                ).joinToString("\n")

                Response.Error(error, it.exception)
            }
        }
    }
