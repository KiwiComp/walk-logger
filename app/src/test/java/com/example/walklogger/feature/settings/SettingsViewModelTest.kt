package com.example.walklogger.feature.settings

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.ThemeMode
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.testing.FakeErrorReporter
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakeUserSettingsRepository
import com.example.walklogger.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository(isGranted = false)
    private val userSettings = FakeUserSettingsRepository()
    private val errorReporter = FakeErrorReporter()

    private val viewModel by lazy {
        SettingsViewModel(
            LocationPermissionUseCase(locationRepository),
            userSettings,
            errorReporter,
        )
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

    @Test
    fun `choosing a theme shows it as selected`() = runTest {
        viewModel.uiState.test {
            assertEquals(ThemeMode.System, awaitItem().themeMode)
            viewModel.setThemeMode(ThemeMode.Dark)
            assertEquals(ThemeMode.Dark, expectMostRecentItem().themeMode)
        }
    }

    @Test
    fun `a failed theme save keeps the current theme and says so`() = runTest {
        viewModel.uiState.test {
            userSettings.failures.failWrites = true
            viewModel.setThemeMode(ThemeMode.Dark)
            val state = expectMostRecentItem()
            assertEquals(ThemeMode.System, state.themeMode)
            assertEquals(R.string.error_save_theme, state.userMessage)
        }
        assertEquals(listOf("Failed to save theme mode"), errorReporter.reported.map { it.second })
    }
}
