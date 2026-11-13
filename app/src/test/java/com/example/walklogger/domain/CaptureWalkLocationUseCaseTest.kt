package com.example.walklogger.domain

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakePlaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CaptureWalkLocationUseCaseTest {

    private val locationRepository = FakeLocationRepository()
    private val placeRepository = FakePlaceRepository()
    private val useCase = CaptureWalkLocationUseCase(locationRepository, placeRepository)
    private val elsewhere = Coordinates(59.86, 17.64)

    @Test
    fun `stores the device position for the walk`() = runTest {
        useCase(walkId = 4)
        assertEquals(
            WalkLocation(4, locationRepository.location!!),
            placeRepository.walkLocation.value,
        )
    }

    @Test
    fun `does nothing without permission`() = runTest {
        locationRepository.isGranted = false
        useCase(walkId = 4)
        assertNull(placeRepository.walkLocation.value)
    }

    @Test
    fun `keeps a position already stored for the same walk`() = runTest {
        placeRepository.walkLocation.value = WalkLocation(4, elsewhere)
        useCase(walkId = 4)
        assertEquals(WalkLocation(4, elsewhere), placeRepository.walkLocation.value)
    }

    @Test
    fun `replaces a position left behind by another walk`() = runTest {
        placeRepository.walkLocation.value = WalkLocation(3, elsewhere)
        useCase(walkId = 4)
        assertEquals(
            WalkLocation(4, locationRepository.location!!),
            placeRepository.walkLocation.value,
        )
    }

    @Test
    fun `a failed save is swallowed`() = runTest {
        placeRepository.failures.failWrites = true
        useCase(walkId = 4)
        assertNull(placeRepository.walkLocation.value)
    }
}
