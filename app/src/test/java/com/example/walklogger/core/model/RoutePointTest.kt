package com.example.walklogger.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutePointTest {

    private val route = listOf(100L, 200L, 300L, 400L).map {
        RoutePoint(it, Coordinates(59.0, 18.0))
    }

    @Test
    fun `points on the start and end are kept and those outside are hidden`() {
        assertEquals(listOf(200L, 300L), route.within(start = 200, end = 300).map { it.time })
    }

    @Test
    fun `without an end every point from the start on is kept`() {
        assertEquals(listOf(300L, 400L), route.within(start = 300, end = null).map { it.time })
    }

    @Test
    fun `the distance follows the path through the points`() {
        val path =
            listOf(Coordinates(59.0, 18.0), Coordinates(59.01, 18.0), Coordinates(59.0, 18.0))
        assertEquals(2 * 1_111.95, path.distanceMeters()!!, 0.1)
    }

    @Test
    fun `a single point has no distance`() {
        assertNull(listOf(Coordinates(59.0, 18.0)).distanceMeters())
    }
}
