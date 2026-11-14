package com.example.walklogger.core.common

import java.io.IOException

/** Records caught errors that would otherwise only surface as a user message. */
interface ErrorReporter {

    /** [message] is fixed English log context, e.g. "Failed to save walk", never shown to the user. */
    fun report(throwable: Throwable, message: String? = null)
}

/** Network calls throw [IOException] whenever the device is offline; reporting those would bury real errors. */
fun ErrorReporter.reportUnlessOffline(throwable: Throwable, message: String) {
    if (throwable !is IOException) report(throwable, message)
}
