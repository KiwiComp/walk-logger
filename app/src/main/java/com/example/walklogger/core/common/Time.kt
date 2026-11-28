package com.example.walklogger.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Epoch millis, as stored for walks, as the local date and time in [zone]. */
fun Long.toLocalDateTime(zone: ZoneId): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()

/** The epoch millis walks are stored with, for this local date and time in [zone]. */
fun LocalDateTime.toEpochMilli(zone: ZoneId): Long = atZone(zone).toInstant().toEpochMilli()

/**
 * Millis that move [start] to the same clock time on [date]; whole local days, so a daylight saving
 * change between the two dates keeps the clock time rather than the duration.
 */
fun dayShiftMillis(start: Long, date: LocalDate, zone: ZoneId): Long {
    val local = start.toLocalDateTime(zone)
    val days = ChronoUnit.DAYS.between(local.toLocalDate(), date)
    return local.plusDays(days).toEpochMilli(zone) - start
}
