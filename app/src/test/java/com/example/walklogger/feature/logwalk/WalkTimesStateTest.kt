package com.example.walklogger.feature.logwalk

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkTimesStateTest {

    private val today = LocalDate.of(2026, 9, 12)
    private val times = WalkTimesState(date = today, now = today.atTime(20, 0))

    @Test
    fun `missing times block saving until both are set`() {
        assertEquals(SaveBlocker.TIMES, times.blocker)
        assertEquals(SaveBlocker.TIMES, times.copy(startTime = LocalTime.of(17, 0)).blocker)
        assertNull(
            times.copy(startTime = LocalTime.of(17, 0), endTime = LocalTime.of(18, 0)).blocker,
        )
    }

    @Test
    fun `an end not after the start blocks saving`() {
        val walk = times.copy(startTime = LocalTime.of(18, 0), endTime = LocalTime.of(18, 0))
        assertEquals(0, walk.durationMinutes)
        assertEquals(SaveBlocker.END_BEFORE_START, walk.blocker)
    }

    @Test
    fun `an end after now blocks saving`() {
        val walk = times.copy(startTime = LocalTime.of(19, 30), endTime = LocalTime.of(20, 1))
        assertEquals(SaveBlocker.IN_FUTURE, walk.blocker)
        assertEquals(SaveBlocker.IN_FUTURE, walk.copy(date = today.plusDays(1)).blocker)
    }

    @Test
    fun `the date picker stops at today`() {
        assertEquals(today, times.maxDate)
    }
}
