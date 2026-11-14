package com.example.walklogger.core.ui.message

import androidx.annotation.StringRes
import com.example.walklogger.core.common.ErrorReporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The snackbar message a ViewModel combines into its UiState, until the UI reports it shown.
 * One at a time; a newer message replaces one not yet shown.
 * Each message comes from a caught error, which is reported to [ErrorReporter] as it is shown.
 * See https://developer.android.com/topic/architecture/ui-layer/events
 */
class UserMessageState(private val errorReporter: ErrorReporter) {

    private val _message = MutableStateFlow<Int?>(null)
    val message: StateFlow<Int?> = _message.asStateFlow()

    fun showAndReportError(
        throwable: Throwable,
        @StringRes userMessageRes: Int,
        logMessage: String,
    ) {
        errorReporter.report(throwable, logMessage)
        show(userMessageRes)
    }

    private fun show(@StringRes message: Int) {
        _message.value = message
    }

    /** Only clears [message] if it is still current, so a newer one is not lost. */
    fun shown(@StringRes message: Int) {
        _message.compareAndSet(message, null)
    }
}
