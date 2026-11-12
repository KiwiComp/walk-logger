package com.example.walklogger.core.location

/** Stores where an active walk is, in a way that survives the screen being locked right after the start. */
interface WalkLocationCapturer {
    /** Does nothing without location permission; it is never asked for here. */
    fun capture(walkId: Long)
}
