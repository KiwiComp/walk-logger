package com.example.walklogger.core.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class WalkDaysTest {

    private val zone = ZoneOffset.ofHours(2)
    private val day = LocalDate.of(2026, 9, 12)

    private fun walkAt(id: Long, date: LocalDate, time: LocalTime) = Walk(
        id = id,
        dogId = 1,
        handlers = listOf("Anna"),
        startTime = date.atTime(time).toInstant(zone).toEpochMilli(),
        endTime = date.atTime(time).plusMinutes(30).toInstant(zone).toEpochMilli(),
    )

    @Test
    fun `the start day and time are read in the given zone`() {
        val justAfterMidnight = walkAt(1, day, LocalTime.of(0, 30))
        assertEquals(day, justAfterMidnight.startDate(zone))
        assertEquals(LocalTime.of(0, 30), justAfterMidnight.startLocalTime(zone))
        assertEquals(day.minusDays(1), justAfterMidnight.startDate(ZoneOffset.UTC))
    }

    @Test
    fun `walks are grouped by start day, each day in start order`() {
        val evening = walkAt(1, day, LocalTime.of(18, 0))
        val morning = walkAt(2, day, LocalTime.of(7, 0))
        val yesterday = walkAt(3, day.minusDays(1), LocalTime.of(12, 0))
        assertEquals(
            mapOf(day to listOf(morning, evening), day.minusDays(1) to listOf(yesterday)),
            listOf(evening, yesterday, morning).groupByStartDate(zone),
        )
    }
}
