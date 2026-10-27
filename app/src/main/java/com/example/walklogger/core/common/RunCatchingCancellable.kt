package com.example.walklogger.core.common

import kotlin.coroutines.cancellation.CancellationException

/** Like [runCatching], but rethrows [CancellationException] so a cancelled scope still stops. */
suspend fun <T> runCatchingCancellable(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
