package com.example.walklogger.feature.logwalk

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** The date and times the after-walk and past-walk forms share. */
data class WalkTimesState(
    val date: LocalDate,
    /** The walk must have ended by now; refreshed on every time edit. */
    val now: LocalDateTime,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
) {
    /** Null until both times are set; zero or negative when the end is not after the start. */
    val durationMinutes: Int? =
        if (startTime != null && endTime != null) {
            Duration.between(startTime, endTime).toMinutes().toInt()
        } else {
            null
        }

    /** Latest day the date picker offers. */
    val maxDate: LocalDate = now.toLocalDate()

    /** What about the times still stops the walk from being saved. */
    val blocker: SaveBlocker? = when {
        durationMinutes == null -> SaveBlocker.TIMES
        durationMinutes <= 0 -> SaveBlocker.END_BEFORE_START
        date.atTime(endTime) > now -> SaveBlocker.IN_FUTURE
        else -> null
    }
}
