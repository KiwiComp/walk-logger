package com.example.walklogger.core.model

import com.example.walklogger.core.common.toLocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** The day the walk started on, in [zone]. */
fun Walk.startDate(zone: ZoneId): LocalDate = startTime.toLocalDateTime(zone).toLocalDate()

/** The time of day the walk started, in [zone]. */
fun Walk.startLocalTime(zone: ZoneId): LocalTime = startTime.toLocalDateTime(zone).toLocalTime()

/** The walks by the day they started on in [zone], each day's walks in start order. */
fun List<Walk>.groupByStartDate(zone: ZoneId): Map<LocalDate, List<Walk>> =
    sortedBy { it.startTime }.groupBy { it.startDate(zone) }
