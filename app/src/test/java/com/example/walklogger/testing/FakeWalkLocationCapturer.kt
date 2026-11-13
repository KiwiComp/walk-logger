package com.example.walklogger.testing

import com.example.walklogger.core.data.WalkLocationCapturer

/** Records the walks a position was requested for. */
class FakeWalkLocationCapturer : WalkLocationCapturer {

    val capturedWalkIds = mutableListOf<Long>()

    override fun capture(walkId: Long) {
        capturedWalkIds += walkId
    }
}
