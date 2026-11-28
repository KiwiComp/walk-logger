package com.example.walklogger.domain

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.RoutePoint
import com.example.walklogger.core.model.Walk
import com.example.walklogger.testing.FakeWalkRouteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveWalkDistancesUseCaseTest {

    private val routeRepository = FakeWalkRouteRepository()

    private fun walk(id: Long) =
        Walk(id = id, dogId = 1, handlers = listOf("Anna"), startTime = 1_000, endTime = 3_000)

    private fun TestScope.distancesOf(vararg walks: Walk) =
        ObserveWalkDistancesUseCase(routeRepository, UnconfinedTestDispatcher(testScheduler))(
            walks.toList(),
        )

    @Test
    fun `counts only the points within the walk's times and leaves out walks without a route`() =
        runTest {
            routeRepository.addPoint(1, RoutePoint(1_000, Coordinates(59.0, 18.0)))
            routeRepository.addPoint(1, RoutePoint(3_000, Coordinates(59.01, 18.0)))
            routeRepository.addPoint(1, RoutePoint(9_000, Coordinates(59.5, 18.0)))

            val distances = distancesOf(walk(1), walk(2)).first()

            assertEquals(setOf(1L), distances.keys)
            assertEquals(1_111.95, distances.getValue(1), 0.1)
        }

    @Test
    fun `a walk that is not asked for gets no distance`() = runTest {
        listOf(1L, 2L).forEach { id ->
            routeRepository.addPoint(id, RoutePoint(1_000, Coordinates(59.0, 18.0)))
            routeRepository.addPoint(id, RoutePoint(3_000, Coordinates(59.01, 18.0)))
        }

        assertEquals(setOf(2L), distancesOf(walk(2)).first().keys)
    }
}
