package com.example.walklogger.testing

import java.io.IOException

/** Shared by the fakes: while [failWrites] is on, every write throws; reads keep working. */
class FailureSwitch {

    var failWrites: Boolean = false

    fun check() {
        if (failWrites) throw IOException("simulated write failure")
    }
}
