package com.example.walklogger.domain

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.RoutePoint
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeErrorReporter
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.FakeWalkRouteRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackWalkLocationUseCaseTest {

    private val locationRepository = FakeLocationRepository()
    private val activeWalkLocationRepository = FakeActiveWalkLocationRepository()
    private val walkRepository = FakeWalkRepository()
    private val walkRouteRepository = FakeWalkRouteRepository()
    private val errorReporter = FakeErrorReporter()
    private val now = Instant.ofEpochMilli(5_000)
    private val useCase =
        TrackWalkLocationUseCase(
            locationRepository,
            activeWalkLocationRepository,
            walkRepository,
            walkRouteRepository,
            Clock.fixed(now, ZoneOffset.UTC),
            errorReporter,
        )
    private val elsewhere = Coordinates(59.86, 17.64)
    private val onTheWay = Coordinates(59.3301, 18.0702)

    @Test
    fun `stores the device position for the walk`() = runTest {
        useCase(walkId = 4)
        assertEquals(
            WalkLocation(4, locationRepository.location!!),
            activeWalkLocationRepository.walkLocation.value,
        )
    }

    @Test
    fun `does nothing without permission`() = runTest {
        locationRepository.isGranted = false
        useCase(walkId = 4)
        assertNull(activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `keeps a position already stored for the same walk`() = runTest {
        activeWalkLocationRepository.walkLocation.value = WalkLocation(4, elsewhere)
        useCase(walkId = 4)
        assertEquals(WalkLocation(4, elsewhere), activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `replaces a position left behind by another walk`() = runTest {
        activeWalkLocationRepository.walkLocation.value = WalkLocation(3, elsewhere)
        useCase(walkId = 4)
        assertEquals(
            WalkLocation(4, locationRepository.location!!),
            activeWalkLocationRepository.walkLocation.value,
        )
    }

    @Test
    fun `a failed save is reported and does not fail the caller`() = runTest {
        activeWalkLocationRepository.failures.failWrites = true
        useCase(walkId = 4)
        assertNull(activeWalkLocationRepository.walkLocation.value)
        assertEquals(
            listOf("Failed to capture walk location"),
            errorReporter.reported.map {
                it.second
            },
        )
    }

    @Test
    fun `stores route points until the walk ends`() = runTest {
        val walkId = startWalk()
        val tracking = launch { useCase(walkId) }
        runCurrent()

        locationRepository.updates.emit(onTheWay)
        walkRepository.endWalk(walkId, notes = "", stool = null)
        runCurrent()
        locationRepository.updates.emit(elsewhere)

        assertTrue(tracking.isCompleted)
        assertEquals(
            listOf(RoutePoint(now.toEpochMilli(), onTheWay)),
            walkRouteRepository.observeRoute(walkId).first(),
        )
    }

    @Test
    fun `stops when the walk is discarded`() = runTest {
        val walkId = startWalk()
        val tracking = launch { useCase(walkId) }
        runCurrent()

        walkRepository.deleteWalk(walkId)
        runCurrent()

        assertTrue(tracking.isCompleted)
    }

    @Test
    fun `a failed route point is reported and the next one is still stored`() = runTest {
        val walkId = startWalk()
        launch { useCase(walkId) }
        runCurrent()

        walkRouteRepository.failures.failWrites = true
        locationRepository.updates.emit(elsewhere)
        walkRouteRepository.failures.failWrites = false
        locationRepository.updates.emit(onTheWay)

        assertEquals(
            listOf(RoutePoint(now.toEpochMilli(), onTheWay)),
            walkRouteRepository.observeRoute(walkId).first(),
        )
        assertEquals(
            listOf("Failed to store route point"),
            errorReporter.reported.map {
                it.second
            },
        )
        walkRepository.endWalk(walkId, notes = "", stool = null)
    }

    private suspend fun startWalk(): Long = walkRepository.startWalk(
        dogId = 1,
        handlers = listOf("Anna"),
        equipment = Equipment.HARNESS,
    )
}
