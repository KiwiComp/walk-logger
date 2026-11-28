package com.example.walklogger.core.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeTest {

    private val zone = ZoneId.of("Europe/Stockholm")

    @Test
    fun `the same date shifts nothing`() {
        val start = LocalDateTime.of(2026, 9, 12, 7, 5).toEpochMilli(zone)
        assertEquals(0L, dayShiftMillis(start, LocalDate.of(2026, 9, 12), zone))
    }

    @Test
    fun `a shift across daylight saving keeps the clock time`() {
        val start = LocalDateTime.of(2026, 3, 28, 7, 5).toEpochMilli(zone)
        val shifted = start + dayShiftMillis(start, LocalDate.of(2026, 3, 30), zone)
        assertEquals(LocalDateTime.of(2026, 3, 30, 7, 5), shifted.toLocalDateTime(zone))
    }
}
