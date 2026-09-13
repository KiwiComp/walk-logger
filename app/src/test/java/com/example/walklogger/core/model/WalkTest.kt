package com.example.walklogger.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkTest {
    @Test
    fun `walk without end time is active`() {
        assertTrue(Walk(id = 1, startedAtMillis = 0, endedAtMillis = null).isActive)
    }

    @Test
    fun `walk with end time is not active`() {
        assertFalse(Walk(id = 1, startedAtMillis = 0, endedAtMillis = 10).isActive)
    }
}
