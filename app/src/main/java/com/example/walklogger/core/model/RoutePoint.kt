package com.example.walklogger.core.model

/** Where the device was at [time], epoch millis, during an active walk. */
data class RoutePoint(val time: Long, val coordinates: Coordinates)

/**
 * The points recorded between [start] and [end], both inclusive; a null [end] keeps every point from
 * [start] on. Points outside are only hidden, so moving a time back out shows them again.
 */
fun List<RoutePoint>.within(start: Long, end: Long?): List<RoutePoint> =
    filter { it.time >= start && (end == null || it.time <= end) }
