package com.example.walklogger.feature.settings

import app.cash.turbine.test
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.testing.FakeErrorReporter
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository(isGranted = false)

    private val viewModel by lazy {
        SettingsViewModel(LocationPermissionUseCase(locationRepository), FakeErrorReporter())
    }

    @Test
    fun `a permission granted in system settings shows once the screen is refreshed`() = runTest {
        viewModel.uiState.test {
            assertFalse(awaitItem().hasLocationPermission)
            locationRepository.isGranted = true
            viewModel.refreshLocationPermission()
            assertTrue(expectMostRecentItem().hasLocationPermission)
        }
    }

    @Test
    fun `answering the dialog closes it and remembers that location was asked for`() = runTest {
        viewModel.uiState.test {
            viewModel.requestLocationPermission()
            assertTrue(expectMostRecentItem().shouldRequestLocationPermission)

            viewModel.onLocationPermissionResult(granted = true)
            with(expectMostRecentItem()) {
                assertFalse(shouldRequestLocationPermission)
                assertTrue(hasLocationPermission)
                assertTrue(wasLocationRequested)
            }
        }
    }
}
