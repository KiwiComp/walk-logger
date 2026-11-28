package com.example.walklogger.core.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Where the device was at [time], epoch millis, during an active walk. */
data class RoutePoint(val time: Long, val coordinates: Coordinates)

/**
 * The points recorded between [start] and [end], both inclusive; a null [end] keeps every point from
 * [start] on. Points outside are only hidden, so moving a time back out shows them again.
 */
fun List<RoutePoint>.within(start: Long, end: Long?): List<RoutePoint> =
    filter { it.time >= start && (end == null || it.time <= end) }

private const val EARTH_RADIUS_METERS = 6_371_000.0

/** Length of the path through the points in order, by the haversine formula; null with fewer than two. */
fun List<Coordinates>.distanceMeters(): Double? {
    if (size < 2) return null
    return zipWithNext().sumOf { (from, to) ->
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        2 * EARTH_RADIUS_METERS * asin(sqrt(a))
    }
}
