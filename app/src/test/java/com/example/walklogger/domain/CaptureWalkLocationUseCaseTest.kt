package com.example.walklogger.domain

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeErrorReporter
import com.example.walklogger.testing.FakeLocationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CaptureWalkLocationUseCaseTest {

    private val locationRepository = FakeLocationRepository()
    private val activeWalkLocationRepository = FakeActiveWalkLocationRepository()
    private val errorReporter = FakeErrorReporter()
    private val useCase =
        CaptureWalkLocationUseCase(locationRepository, activeWalkLocationRepository, errorReporter)
    private val elsewhere = Coordinates(59.86, 17.64)

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
}
