package com.example.walklogger.core.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/** Epoch millis, as stored for walks, as the local date and time in [zone]. */
fun Long.toLocalDateTime(zone: ZoneId): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()

/** The epoch millis walks are stored with, for this local date and time in [zone]. */
fun LocalDateTime.toEpochMilli(zone: ZoneId): Long = atZone(zone).toInstant().toEpochMilli()
