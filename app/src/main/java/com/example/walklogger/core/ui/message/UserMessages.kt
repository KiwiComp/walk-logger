package com.example.walklogger.core.ui.message

import androidx.annotation.StringRes
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * App-wide queue of snackbar messages. App-scoped so a message posted by a screen that is
 * closing still shows on the screen it returns to; WalkLoggerApp is the only collector.
 */
@Singleton
class UserMessages @Inject constructor() {

    private val queue = Channel<Int>(Channel.UNLIMITED)

    /** Each message is delivered once. */
    val messages: Flow<Int> = queue.receiveAsFlow()

    fun show(@StringRes message: Int) {
        queue.trySend(message)
    }
}
