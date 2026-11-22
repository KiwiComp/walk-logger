package com.example.walklogger.core.data

/** Records an active walk's start position and route for as long as it lasts, also with the screen locked. */
interface WalkTracker {
    /**
     * Does nothing without location permission, which is never asked for here, and nothing more when
     * the walk is already being tracked, so it is safe to call each time the walk is shown.
     */
    fun track(walkId: Long)
}
