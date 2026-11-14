package com.example.walklogger.core.common

import javax.inject.Inject

/** Release builds log nothing: caught errors can carry personal or location data. */
class NoOpErrorReporter @Inject constructor() : ErrorReporter {

    override fun report(throwable: Throwable, message: String?) = Unit
}
