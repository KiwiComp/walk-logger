package com.example.walklogger.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkTest {
    private fun walk(endTime: Long? = null, rating: Int? = null) =
        Walk(id = 1, handler = "Kim", startTime = 0, endTime = endTime, rating = rating)

    @Test
    fun `walk without end time is active`() {
        assertTrue(walk(endTime = null).isActive)
    }

    @Test
    fun `walk with end time is not active`() {
        assertFalse(walk(endTime = 10).isActive)
    }

    @Test
    fun `rating within 1 to 7 is accepted`() {
        walk(rating = 1)
        walk(rating = 7)
    }

    @Test
    fun `rating outside 1 to 7 is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { walk(rating = 0) }
        assertThrows(IllegalArgumentException::class.java) { walk(rating = 8) }
    }
}
