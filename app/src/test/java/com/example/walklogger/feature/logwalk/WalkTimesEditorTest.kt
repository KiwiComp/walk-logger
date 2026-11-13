package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Walk
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkTimesEditorTest {

    private val zone = ZoneOffset.ofHours(2)
    private val date = LocalDate.of(2026, 9, 12)
    private val clock = Clock.fixed(date.atTime(20, 0).toInstant(zone), zone)
    private val notified = mutableListOf<Pair<Long, Long>>()
    private val editor = WalkTimesEditor(clock, date) { start, end -> notified += start to end }

    private fun millis(day: LocalDate, time: LocalTime) =
        day.atTime(time).toInstant(zone).toEpochMilli()

    @Test
    fun `passes both times on once they are set, and again when they change`() {
        editor.setStartTime(LocalTime.of(17, 40))
        assertTrue(notified.isEmpty())
        assertNull(editor.endMillis)
        editor.setEndTime(LocalTime.of(18, 5))
        editor.setDate(date.minusDays(1))
        assertEquals(
            listOf(
                millis(date, LocalTime.of(17, 40)) to millis(date, LocalTime.of(18, 5)),
                millis(date.minusDays(1), LocalTime.of(17, 40)) to
                    millis(date.minusDays(1), LocalTime.of(18, 5)),
            ),
            notified,
        )
        assertEquals(25, editor.state.value.durationMinutes)
    }

    @Test
    fun `prefill takes the walk's date and times in the clock's zone`() {
        val start = millis(date.minusDays(2), LocalTime.of(8, 15))
        val end = millis(date.minusDays(2), LocalTime.of(8, 50))
        editor.prefillFrom(
            Walk(id = 1, dogId = 1, handlers = listOf("Anna"), startTime = start, endTime = end),
        )
        val state = editor.state.value
        assertEquals(date.minusDays(2), state.date)
        assertEquals(LocalTime.of(8, 15), state.startTime)
        assertEquals(LocalTime.of(8, 50), state.endTime)
        assertEquals(listOf(start to end), notified)
    }
}
