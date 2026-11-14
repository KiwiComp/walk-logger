package com.example.walklogger.feature.logwalk

import com.example.walklogger.R
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeErrorReporter
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakeWeatherRepository
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherFormEditorTest {

    private val shown = mutableListOf<Int>()
    private val logged = mutableListOf<String>()
    private val errorReporter = FakeErrorReporter()
    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val now = Instant.parse("2026-11-09T12:00:00Z")
    private val start = now.minus(Duration.ofHours(2)).toEpochMilli()
    private val end = now.minus(Duration.ofHours(1)).toEpochMilli()
    private val fetched = weatherRepository.weather

    private fun runEditorTest(body: suspend TestScope.(WeatherFormEditor) -> Unit) =
        runTest(UnconfinedTestDispatcher()) {
            body(
                WeatherFormEditor(
                    backgroundScope,
                    FetchWalkWeatherUseCase(
                        locationRepository,
                        weatherRepository,
                        FakeActiveWalkLocationRepository(),
                        errorReporter,
                    ),
                    LocationPermissionUseCase(locationRepository),
                    locationRepository,
                    { _, message, log ->
                        shown += message
                        logged += log
                    },
                    errorReporter,
                ),
            )
        }

    @Test
    fun `prefill shows stored weather and opens the section only when there is some`() =
        runEditorTest { editor ->
            editor.prefillFrom(null)
            assertFalse(editor.state.value.isExpanded)
            editor.prefillFrom(
                WalkWeather(
                    condition = WeatherCondition.LIGHT_SNOW,
                    temperatureCelsius = -2.0,
                    snowfallCm = 0.3,
                ),
            )
            with(editor.state.value) {
                assertEquals(WeatherCondition.LIGHT_SNOW, condition)
                assertEquals(-2.0, temperatureCelsius!!, 0.0)
                assertEquals(0.3, fetchedWeather?.snowfallCm)
                assertTrue(isExpanded)
            }
        }

    @Test
    fun `new times fetch the weather and fill in the temperature and condition`() =
        runEditorTest { editor ->
            editor.onTimesChanged(start, end, autoFetch = true)
            with(editor.state.value) {
                assertEquals(fetched, fetchedWeather)
                assertEquals(7.5, temperatureCelsius!!, 0.0)
                assertEquals(WeatherCondition.LIGHT_RAIN, condition)
                assertEquals(WeatherStatus.IDLE, status)
                assertTrue(isExpanded)
                assertEquals(fetched, walkWeather)
            }
            assertEquals(
                Instant.ofEpochMilli(start + (end - start) / 2),
                weatherRepository.requests.single().second,
            )
        }

    @Test
    fun `without auto fetch the times are only kept for the fetch button`() =
        runEditorTest { editor ->
            editor.onTimesChanged(start, end, autoFetch = false)
            assertTrue(weatherRepository.requests.isEmpty())
            editor.fetchWeather()
            assertEquals(fetched, editor.state.value.fetchedWeather)
        }

    @Test
    fun `a manual change stops new times from refetching, but the button still fetches`() =
        runEditorTest { editor ->
            editor.onTimesChanged(start, end, autoFetch = true)
            editor.setTemperature("3,5")
            editor.onTimesChanged(start - 60_000, end, autoFetch = true)
            assertEquals(1, weatherRepository.requests.size)
            assertEquals(3.5, editor.state.value.temperatureCelsius!!, 0.0)
            editor.fetchWeather()
            assertEquals(2, weatherRepository.requests.size)
            assertFalse(editor.state.value.isEditedManually)
        }

    @Test
    fun `the first fetch without permission asks for it, then falls back to the saved place`() =
        runEditorTest { editor ->
            locationRepository.isGranted = false
            val uppsala = Place("Uppsala", null, Coordinates(59.86, 17.64))
            locationRepository.savePlace(uppsala)
            editor.onTimesChanged(start, end, autoFetch = true)
            assertTrue(editor.state.value.shouldRequestLocationPermission)
            assertTrue(weatherRepository.requests.isEmpty())

            editor.onLocationPermissionResult(granted = false)
            with(editor.state.value) {
                assertFalse(shouldRequestLocationPermission)
                assertFalse(hasLocationPermission)
                assertEquals(uppsala, place)
                assertEquals(fetched, fetchedWeather)
            }
            assertEquals(uppsala.coordinates, weatherRepository.requests.single().first)

            editor.fetchWeather()
            assertFalse(editor.state.value.shouldRequestLocationPermission)
        }

    @Test
    fun `without location or place the section says to choose a place, and choosing one fetches`() =
        runEditorTest { editor ->
            locationRepository.isGranted = false
            locationRepository.markLocationPermissionRequested()
            val uppsala = Place("Uppsala", "Uppsala län", Coordinates(59.86, 17.64))
            locationRepository.searchResults = listOf(uppsala)
            editor.onTimesChanged(start, end, autoFetch = true)
            assertEquals(WeatherStatus.NO_LOCATION, editor.state.value.status)

            editor.openPlaceSearch()
            editor.searchPlaces("Upps")
            assertEquals(listOf(uppsala), editor.state.value.placeSearch?.results)
            editor.selectPlace(uppsala)
            with(editor.state.value) {
                assertNull(placeSearch)
                assertEquals(uppsala, place)
                assertEquals(fetched, fetchedWeather)
                assertEquals(WeatherStatus.IDLE, status)
            }
        }

    @Test
    fun `a failed place search says so`() = runEditorTest { editor ->
        locationRepository.failSearches = true
        editor.openPlaceSearch()
        editor.searchPlaces("Upps")
        assertTrue(editor.state.value.placeSearch!!.isFailed)
        assertTrue(errorReporter.reported.isEmpty())
        editor.closePlaceSearch()
        assertNull(editor.state.value.placeSearch)
    }

    @Test
    fun `walks older than the forecast reaches and failed fetches leave the weather to the handler`() =
        runEditorTest { editor ->
            weatherRepository.oldestAvailable = now.minus(Duration.ofDays(92))
            val old = now.minus(Duration.ofDays(100)).toEpochMilli()
            editor.onTimesChanged(old, old + 3_600_000, autoFetch = true)
            assertEquals(WeatherStatus.TOO_OLD, editor.state.value.status)

            weatherRepository.fail = true
            editor.onTimesChanged(start, end, autoFetch = true)
            with(editor.state.value) {
                assertEquals(WeatherStatus.FAILED, status)
                assertNull(fetchedWeather)
            }
        }

    @Test
    fun `the temperature takes a decimal comma and the condition toggles`() =
        runEditorTest { editor ->
            editor.setTemperature("-12,5°")
            assertEquals("-12,5", editor.state.value.temperatureInput)
            assertEquals(-12.5, editor.state.value.temperatureCelsius!!, 0.0)
            editor.selectCondition(WeatherCondition.SNOW)
            assertEquals(WeatherCondition.SNOW, editor.state.value.condition)
            editor.selectCondition(WeatherCondition.SNOW)
            assertNull(editor.state.value.condition)
            assertTrue(editor.state.value.isEditedManually)
        }

    @Test
    fun `clear weather drops everything and keeps new times from refetching`() =
        runEditorTest { editor ->
            editor.onTimesChanged(start, end, autoFetch = true)
            editor.clearWeather()
            with(editor.state.value) {
                assertNull(fetchedWeather)
                assertEquals("", temperatureInput)
                assertNull(condition)
                assertNull(walkWeather)
            }
            editor.onTimesChanged(start - 60_000, end, autoFetch = true)
            assertEquals(1, weatherRepository.requests.size)
        }

    @Test
    fun `the fetch button hides once the weather is fetched for the walk's times`() =
        runEditorTest { editor ->
            assertFalse(editor.state.value.showsFetch)
            editor.onTimesChanged(start, end, autoFetch = false)
            assertTrue(editor.state.value.showsFetch)
            editor.fetchWeather()
            assertFalse(editor.state.value.showsFetch)
        }

    @Test
    fun `the fetch button returns when the times change after a manual edit, or after clearing`() =
        runEditorTest { editor ->
            editor.onTimesChanged(start, end, autoFetch = true)
            editor.setTemperature("4")
            assertFalse(editor.state.value.showsFetch)
            editor.onTimesChanged(start - 60_000, end, autoFetch = true)
            assertTrue(editor.state.value.showsFetch)
            editor.fetchWeather()
            assertFalse(editor.state.value.showsFetch)
            editor.clearWeather()
            assertTrue(editor.state.value.showsFetch)
        }

    @Test
    fun `a failed fetch offers the button, a too old walk or a missing location does not`() =
        runEditorTest { editor ->
            weatherRepository.fail = true
            editor.onTimesChanged(start, end, autoFetch = true)
            assertTrue(editor.state.value.showsFetch)

            weatherRepository.oldestAvailable = now.minus(Duration.ofDays(92))
            val old = now.minus(Duration.ofDays(100)).toEpochMilli()
            editor.onTimesChanged(old, old + 3_600_000, autoFetch = true)
            assertFalse(editor.state.value.showsFetch)

            locationRepository.isGranted = false
            locationRepository.markLocationPermissionRequested()
            editor.onTimesChanged(start, end, autoFetch = true)
            assertEquals(WeatherStatus.NO_LOCATION, editor.state.value.status)
            assertFalse(editor.state.value.showsFetch)
        }

    @Test
    fun `stored fetched weather counts as current until the times change`() =
        runEditorTest { editor ->
            editor.prefillFrom(fetched)
            editor.onTimesChanged(start, end, autoFetch = false)
            assertFalse(editor.state.value.showsFetch)
            editor.onTimesChanged(start - 60_000, end, autoFetch = false)
            assertTrue(editor.state.value.showsFetch)
        }

    @Test
    fun `stored weather entered by hand still offers the button`() = runEditorTest { editor ->
        editor.prefillFrom(WalkWeather(condition = WeatherCondition.FOG, temperatureCelsius = 3.0))
        editor.onTimesChanged(start, end, autoFetch = false)
        assertTrue(editor.state.value.showsFetch)
    }

    @Test
    fun `fetched weather hides the manual fields until edit opens them`() =
        runEditorTest { editor ->
            assertTrue(editor.state.value.showsManualEntry)
            editor.onTimesChanged(start, end, autoFetch = true)
            assertFalse(editor.state.value.showsManualEntry)
            editor.toggleWeatherEditing()
            assertTrue(editor.state.value.showsManualEntry)
            editor.setTemperature("4")
            editor.toggleWeatherEditing()
            assertFalse(editor.state.value.showsManualEntry)
            assertEquals(4.0, editor.state.value.walkWeather?.temperatureCelsius!!, 0.0)
        }

    @Test
    fun `a new fetch ends editing`() = runEditorTest { editor ->
        editor.onTimesChanged(start, end, autoFetch = true)
        editor.toggleWeatherEditing()
        editor.fetchWeather()
        assertFalse(editor.state.value.isEditing)
        assertFalse(editor.state.value.showsManualEntry)
    }

    @Test
    fun `without fetched weather the manual fields show directly`() = runEditorTest { editor ->
        weatherRepository.fail = true
        editor.onTimesChanged(start, end, autoFetch = true)
        assertTrue(editor.state.value.showsManualEntry)

        val old = now.minus(Duration.ofDays(100)).toEpochMilli()
        editor.onTimesChanged(old, old + 3_600_000, autoFetch = true)
        assertTrue(editor.state.value.showsManualEntry)

        weatherRepository.fail = false
        editor.onTimesChanged(start, end, autoFetch = true)
        editor.toggleWeatherEditing()
        editor.clearWeather()
        assertFalse(editor.state.value.isEditing)
        assertTrue(editor.state.value.showsManualEntry)
    }

    @Test
    fun `a place that cannot be saved says so`() = runEditorTest { editor ->
        locationRepository.failures.failWrites = true
        editor.selectPlace(Place("Uppsala", null, Coordinates(59.86, 17.64)))
        assertEquals(listOf(R.string.error_save_place), shown)
        assertEquals(listOf("Failed to save place"), logged)
    }
}
