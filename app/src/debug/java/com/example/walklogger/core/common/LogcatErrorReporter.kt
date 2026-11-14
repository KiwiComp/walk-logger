package com.example.walklogger.core.common

import android.util.Log
import javax.inject.Inject

class LogcatErrorReporter @Inject constructor() : ErrorReporter {

    override fun report(throwable: Throwable, message: String?) {
        Log.w(TAG, message ?: DEFAULT_MESSAGE, throwable)
    }

    private companion object {
        const val TAG = "WalkLogger"
        const val DEFAULT_MESSAGE = "Caught error"
    }
}
