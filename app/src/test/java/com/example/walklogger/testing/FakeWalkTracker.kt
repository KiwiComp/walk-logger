package com.example.walklogger.testing

import com.example.walklogger.core.data.WalkTracker

/** Records the walks tracking was requested for, once per call. */
class FakeWalkTracker : WalkTracker {

    val trackedWalkIds = mutableListOf<Long>()

    override fun track(walkId: Long) {
        trackedWalkIds += walkId
    }
}
