package com.example.walklogger.testing

import com.example.walklogger.core.common.ErrorReporter

/** Records reported errors so tests can assert a failure was not swallowed. */
class FakeErrorReporter : ErrorReporter {

    val reported = mutableListOf<Pair<Throwable, String?>>()

    override fun report(throwable: Throwable, message: String?) {
        reported += throwable to message
    }
}
