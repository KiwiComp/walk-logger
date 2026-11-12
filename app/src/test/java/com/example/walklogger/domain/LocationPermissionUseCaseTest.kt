package com.example.walklogger.domain

import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakePlaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationPermissionUseCaseTest {

    private val locationRepository = FakeLocationRepository(isGranted = false)
    private val placeRepository = FakePlaceRepository()
    private val useCase = LocationPermissionUseCase(locationRepository, placeRepository)

    @Test
    fun `asks for location only until it has asked once`() = runTest {
        assertTrue(useCase.shouldRequest())
        useCase.markRequested()
        assertFalse(useCase.shouldRequest())
    }

    @Test
    fun `does not ask when location is already granted`() = runTest {
        locationRepository.isGranted = true
        assertTrue(useCase.hasPermission())
        assertFalse(useCase.shouldRequest())
    }
}
