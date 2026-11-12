package com.example.walklogger.feature.logwalk

import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.testing.FakeBehaviourIssueRepository
import com.example.walklogger.testing.FakeLocationProvider
import com.example.walklogger.testing.FakeMedicalConcernRepository
import com.example.walklogger.testing.FakePlaceRepository
import com.example.walklogger.testing.FakeWeatherRepository
import com.example.walklogger.testing.shownMessages
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkFormEditorTest {

    private val behaviourIssueRepository = FakeBehaviourIssueRepository()
    private val medicalConcernRepository = FakeMedicalConcernRepository()
    private val userMessages = UserMessages()
    private val locationProvider = FakeLocationProvider()
    private val placeRepository = FakePlaceRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val now = Instant.parse("2026-11-09T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val walk =
        Walk(id = 42, dogId = 7, handlers = listOf("Jonas"), startTime = 1_000, endTime = 2_000)
    private val start = now.minus(Duration.ofHours(2)).toEpochMilli()
    private val end = now.minus(Duration.ofHours(1)).toEpochMilli()
    private val fetched = weatherRepository.weather

    private fun runEditorTest(body: suspend TestScope.(WalkFormEditor) -> Unit) =
        runTest(UnconfinedTestDispatcher()) {
            val fetchWalkWeather =
                FetchWalkWeatherUseCase(locationProvider, placeRepository, weatherRepository, clock)
            body(
                WalkFormEditor(
                    backgroundScope,
                    behaviourIssueRepository,
                    medicalConcernRepository,
                    userMessages,
                    fetchWalkWeather,
                    LocationPermissionUseCase(locationProvider, placeRepository),
                    placeRepository,
                ),
            )
        }

    @Test
    fun `starts blank with collar and the stored issues and concerns`() {
        behaviourIssueRepository.addBehaviourIssues("Pulling")
        medicalConcernRepository.addMedicalConcerns("Limping")
        runEditorTest { editor ->
            val form = editor.state.value
            assertNull(form.rating)
            assertNull(form.stool)
            assertEquals(Equipment.COLLAR, form.equipment)
            assertEquals(listOf(BehaviourIssue(1, "Pulling")), form.behaviourIssues)
            assertEquals(listOf(MedicalConcern(1, "Limping")), form.medicalConcerns)
            assertFalse(form.isOptionalRatingsExpanded)
            assertFalse(form.isWeatherExpanded)
            assertFalse(form.isBehaviourIssuesExpanded)
            assertFalse(form.isMedicalConcernsExpanded)
        }
    }

    @Test
    fun `optional sections toggle open and closed`() = runEditorTest { editor ->
        editor.toggleOptionalRatings()
        editor.toggleWeather()
        editor.toggleBehaviourIssuesExpanded()
        editor.toggleMedicalConcernsExpanded()
        with(editor.state.value) {
            assertTrue(isOptionalRatingsExpanded)
            assertTrue(isWeatherExpanded)
            assertTrue(isBehaviourIssuesExpanded)
            assertTrue(isMedicalConcernsExpanded)
        }
        editor.toggleOptionalRatings()
        editor.toggleWeather()
        editor.toggleBehaviourIssuesExpanded()
        editor.toggleMedicalConcernsExpanded()
        with(editor.state.value) {
            assertFalse(isOptionalRatingsExpanded)
            assertFalse(isWeatherExpanded)
            assertFalse(isBehaviourIssuesExpanded)
            assertFalse(isMedicalConcernsExpanded)
        }
    }

    @Test
    fun `an optional rating can be set, replaced, removed and cleared`() = runEditorTest { editor ->
        editor.setOptionalRating(OptionalRating.REACTIVITY, 3)
        editor.setOptionalRating(OptionalRating.DISTRACTION, 7)
        assertEquals(
            mapOf(OptionalRating.REACTIVITY to 3, OptionalRating.DISTRACTION to 7),
            editor.state.value.optionalRatings,
        )
        editor.setOptionalRating(OptionalRating.REACTIVITY, 5)
        editor.setOptionalRating(OptionalRating.DISTRACTION, null)
        assertEquals(mapOf(OptionalRating.REACTIVITY to 5), editor.state.value.optionalRatings)
        editor.clearOptionalRatings()
        assertTrue(editor.state.value.optionalRatings.isEmpty())
    }

    @Test
    fun `stool description only applies after yes, and no drops it`() = runEditorTest { editor ->
        editor.selectStoolQuantity(StoolQuantity.SMALL)
        assertNull(editor.state.value.stool)
        editor.selectStoolPassed(true)
        editor.selectStoolQuantity(StoolQuantity.LARGE)
        editor.selectStoolTexture(StoolTexture.SOFT)
        editor.selectStoolColour(StoolColour.GREY)
        assertEquals(
            Stool(true, StoolQuantity.LARGE, StoolTexture.SOFT, StoolColour.GREY),
            editor.state.value.stool,
        )
        editor.selectStoolColour(null)
        editor.selectStoolPassed(true)
        assertEquals(Stool(true, StoolQuantity.LARGE, StoolTexture.SOFT), editor.state.value.stool)
        editor.selectStoolPassed(false)
        assertEquals(Stool(passed = false), editor.state.value.stool)
        editor.clearStool()
        assertNull(editor.state.value.stool)
    }

    @Test
    fun `yes opens the stool description, done folds it and edit opens it again`() =
        runEditorTest { editor ->
            editor.selectStoolPassed(true)
            assertTrue(editor.state.value.isStoolExpanded)
            editor.selectStoolQuantity(StoolQuantity.NORMAL)
            editor.foldStool()
            assertFalse(editor.state.value.isStoolExpanded)
            assertEquals(
                Stool(passed = true, quantity = StoolQuantity.NORMAL),
                editor.state.value.stool,
            )
            editor.selectStoolPassed(true)
            assertFalse(editor.state.value.isStoolExpanded)
            editor.expandStool()
            assertTrue(editor.state.value.isStoolExpanded)
        }

    @Test
    fun `new times fetch the weather and fill in the temperature and condition`() =
        runEditorTest { editor ->
            editor.onTimesChanged(start, end, autoFetch = true)
            with(editor.state.value) {
                assertEquals(fetched, fetchedWeather)
                assertEquals(7.5, temperatureCelsius!!, 0.0)
                assertEquals(WeatherCondition.LIGHT_RAIN, condition)
                assertEquals(WeatherStatus.IDLE, weatherStatus)
                assertTrue(isWeatherExpanded)
                assertEquals(fetched, weather)
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
            assertFalse(editor.state.value.isWeatherEditedManually)
        }

    @Test
    fun `the first fetch without permission asks for it, then falls back to the saved place`() =
        runEditorTest { editor ->
            locationProvider.isGranted = false
            val uppsala = Place("Uppsala", null, Coordinates(59.86, 17.64))
            placeRepository.savePlace(uppsala)
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
            locationProvider.isGranted = false
            placeRepository.markLocationPermissionRequested()
            val uppsala = Place("Uppsala", "Uppsala län", Coordinates(59.86, 17.64))
            placeRepository.searchResults = listOf(uppsala)
            editor.onTimesChanged(start, end, autoFetch = true)
            assertEquals(WeatherStatus.NO_LOCATION, editor.state.value.weatherStatus)

            editor.openPlaceSearch()
            editor.searchPlaces("Upps")
            assertEquals(listOf(uppsala), editor.state.value.placeSearch?.results)
            editor.selectPlace(uppsala)
            with(editor.state.value) {
                assertNull(placeSearch)
                assertEquals(uppsala, place)
                assertEquals(fetched, fetchedWeather)
                assertEquals(WeatherStatus.IDLE, weatherStatus)
            }
        }

    @Test
    fun `a failed place search says so`() = runEditorTest { editor ->
        placeRepository.failSearches = true
        editor.openPlaceSearch()
        editor.searchPlaces("Upps")
        assertTrue(editor.state.value.placeSearch!!.isFailed)
        editor.closePlaceSearch()
        assertNull(editor.state.value.placeSearch)
    }

    @Test
    fun `walks older than the forecast reaches and failed fetches leave the weather to the handler`() =
        runEditorTest { editor ->
            val old = now.minus(Duration.ofDays(100)).toEpochMilli()
            editor.onTimesChanged(old, old + 3_600_000, autoFetch = true)
            assertEquals(WeatherStatus.TOO_OLD, editor.state.value.weatherStatus)

            weatherRepository.fail = true
            editor.onTimesChanged(start, end, autoFetch = true)
            with(editor.state.value) {
                assertEquals(WeatherStatus.FAILED, weatherStatus)
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
            assertTrue(editor.state.value.isWeatherEditedManually)
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
                assertNull(weather)
            }
            editor.onTimesChanged(start - 60_000, end, autoFetch = true)
            assertEquals(1, weatherRepository.requests.size)
        }

    @Test
    fun `the fetch button hides once the weather is fetched for the walk's times`() =
        runEditorTest { editor ->
            assertFalse(editor.state.value.showsFetchWeather)
            editor.onTimesChanged(start, end, autoFetch = false)
            assertTrue(editor.state.value.showsFetchWeather)
            editor.fetchWeather()
            assertFalse(editor.state.value.showsFetchWeather)
        }

    @Test
    fun `the fetch button returns when the times change after a manual edit, or after clearing`() =
        runEditorTest { editor ->
            editor.onTimesChanged(start, end, autoFetch = true)
            editor.setTemperature("4")
            assertFalse(editor.state.value.showsFetchWeather)
            editor.onTimesChanged(start - 60_000, end, autoFetch = true)
            assertTrue(editor.state.value.showsFetchWeather)
            editor.fetchWeather()
            assertFalse(editor.state.value.showsFetchWeather)
            editor.clearWeather()
            assertTrue(editor.state.value.showsFetchWeather)
        }

    @Test
    fun `a failed fetch offers the button, a too old walk or a missing location does not`() =
        runEditorTest { editor ->
            weatherRepository.fail = true
            editor.onTimesChanged(start, end, autoFetch = true)
            assertTrue(editor.state.value.showsFetchWeather)

            val old = now.minus(Duration.ofDays(100)).toEpochMilli()
            editor.onTimesChanged(old, old + 3_600_000, autoFetch = true)
            assertFalse(editor.state.value.showsFetchWeather)

            locationProvider.isGranted = false
            placeRepository.markLocationPermissionRequested()
            editor.onTimesChanged(start, end, autoFetch = true)
            assertEquals(WeatherStatus.NO_LOCATION, editor.state.value.weatherStatus)
            assertFalse(editor.state.value.showsFetchWeather)
        }

    @Test
    fun `stored fetched weather counts as current until the times change`() =
        runEditorTest { editor ->
            editor.prefillFrom(walk.copy(weather = fetched))
            editor.onTimesChanged(start, end, autoFetch = false)
            assertFalse(editor.state.value.showsFetchWeather)
            editor.onTimesChanged(start - 60_000, end, autoFetch = false)
            assertTrue(editor.state.value.showsFetchWeather)
        }

    @Test
    fun `stored weather entered by hand still offers the button`() = runEditorTest { editor ->
        editor.prefillFrom(
            walk.copy(
                weather = WalkWeather(condition = WeatherCondition.FOG, temperatureCelsius = 3.0),
            ),
        )
        editor.onTimesChanged(start, end, autoFetch = false)
        assertTrue(editor.state.value.showsFetchWeather)
    }

    @Test
    fun `fetched weather hides the manual fields until edit opens them`() =
        runEditorTest { editor ->
            assertTrue(editor.state.value.showsManualWeather)
            editor.onTimesChanged(start, end, autoFetch = true)
            assertFalse(editor.state.value.showsManualWeather)
            editor.toggleWeatherEditing()
            assertTrue(editor.state.value.showsManualWeather)
            editor.setTemperature("4")
            editor.toggleWeatherEditing()
            assertFalse(editor.state.value.showsManualWeather)
            assertEquals(4.0, editor.state.value.weather?.temperatureCelsius!!, 0.0)
        }

    @Test
    fun `a new fetch ends editing`() = runEditorTest { editor ->
        editor.onTimesChanged(start, end, autoFetch = true)
        editor.toggleWeatherEditing()
        editor.fetchWeather()
        assertFalse(editor.state.value.isWeatherEditing)
        assertFalse(editor.state.value.showsManualWeather)
    }

    @Test
    fun `without fetched weather the manual fields show directly`() = runEditorTest { editor ->
        weatherRepository.fail = true
        editor.onTimesChanged(start, end, autoFetch = true)
        assertTrue(editor.state.value.showsManualWeather)

        val old = now.minus(Duration.ofDays(100)).toEpochMilli()
        editor.onTimesChanged(old, old + 3_600_000, autoFetch = true)
        assertTrue(editor.state.value.showsManualWeather)

        weatherRepository.fail = false
        editor.onTimesChanged(start, end, autoFetch = true)
        editor.toggleWeatherEditing()
        editor.clearWeather()
        assertFalse(editor.state.value.isWeatherEditing)
        assertTrue(editor.state.value.showsManualWeather)
    }

    @Test
    fun `issues toggle on and off, clear together, and an added one is stored and selected`() {
        behaviourIssueRepository.addBehaviourIssues("Pulling", "Barking")
        runEditorTest { editor ->
            editor.toggleBehaviourIssue(BehaviourIssue(1, "Pulling"))
            editor.toggleBehaviourIssue(BehaviourIssue(2, "Barking"))
            assertEquals(setOf(1L, 2L), editor.state.value.selectedBehaviourIssueIds)
            editor.toggleBehaviourIssue(BehaviourIssue(1, "Pulling"))
            assertEquals(setOf(2L), editor.state.value.selectedBehaviourIssueIds)
            editor.clearBehaviourIssues()
            assertTrue(editor.state.value.selectedBehaviourIssueIds.isEmpty())
            editor.addBehaviourIssue("  Digging ")
            assertEquals(BehaviourIssue(3, "Digging"), editor.state.value.behaviourIssues.last())
            assertEquals(setOf(3L), editor.state.value.selectedBehaviourIssueIds)
        }
    }

    @Test
    fun `concerns toggle on and off, clear together, and an added one is stored and selected`() {
        medicalConcernRepository.addMedicalConcerns("Limping", "Vomit")
        runEditorTest { editor ->
            editor.toggleMedicalConcern(MedicalConcern(1, "Limping"))
            editor.toggleMedicalConcern(MedicalConcern(2, "Vomit"))
            assertEquals(setOf(1L, 2L), editor.state.value.selectedMedicalConcernIds)
            editor.toggleMedicalConcern(MedicalConcern(1, "Limping"))
            assertEquals(setOf(2L), editor.state.value.selectedMedicalConcernIds)
            editor.clearMedicalConcerns()
            assertTrue(editor.state.value.selectedMedicalConcernIds.isEmpty())
            editor.addMedicalConcern("  Coughing ")
            assertEquals(MedicalConcern(3, "Coughing"), editor.state.value.medicalConcerns.last())
            assertEquals(setOf(3L), editor.state.value.selectedMedicalConcernIds)
        }
    }

    @Test
    fun `notes are cut at the maximum length`() = runEditorTest { editor ->
        editor.setNotes("x".repeat(MAX_NOTES_LENGTH + 1))
        assertEquals(MAX_NOTES_LENGTH, editor.state.value.notes.length)
    }

    @Test
    fun `prefill copies the walk and opens the sections that have content, with the stool folded`() =
        runEditorTest { editor ->
            val stool =
                Stool(
                    passed = true,
                    quantity = StoolQuantity.NORMAL,
                    colour = StoolColour.DARK_BROWN,
                )
            editor.prefillFrom(
                walk.copy(
                    notes = "Muddy",
                    equipment = Equipment.HARNESS,
                    rating = 3,
                    weather = WalkWeather(
                        condition = WeatherCondition.LIGHT_SNOW,
                        temperatureCelsius = -2.0,
                        snowfallCm = 0.3,
                    ),
                    behaviourIssues = listOf(BehaviourIssue(1, "Pulling")),
                    medicalConcerns = listOf(MedicalConcern(4, "Limping")),
                    optionalRatings = mapOf(OptionalRating.RESPONSIVENESS to 4),
                    stool = stool,
                ),
            )
            with(editor.state.value) {
                assertEquals(3, rating)
                assertEquals(Equipment.HARNESS, equipment)
                assertEquals(WeatherCondition.LIGHT_SNOW, condition)
                assertEquals(-2.0, temperatureCelsius!!, 0.0)
                assertEquals(0.3, fetchedWeather?.snowfallCm)
                assertTrue(isWeatherExpanded)
                assertEquals(mapOf(OptionalRating.RESPONSIVENESS to 4), optionalRatings)
                assertTrue(isOptionalRatingsExpanded)
                assertEquals(stool, this.stool)
                assertFalse(isStoolExpanded)
                assertEquals(setOf(1L), selectedBehaviourIssueIds)
                assertTrue(isBehaviourIssuesExpanded)
                assertEquals(setOf(4L), selectedMedicalConcernIds)
                assertTrue(isMedicalConcernsExpanded)
                assertEquals("Muddy", notes)
            }
        }

    @Test
    fun `prefill leaves empty sections closed and keeps the equipment when the walk has none`() =
        runEditorTest { editor ->
            editor.selectEquipment(Equipment.HARNESS)
            editor.prefillFrom(walk)
            with(editor.state.value) {
                assertEquals(Equipment.HARNESS, equipment)
                assertFalse(isOptionalRatingsExpanded)
                assertFalse(isWeatherExpanded)
                assertFalse(isBehaviourIssuesExpanded)
                assertFalse(isMedicalConcernsExpanded)
            }
        }

    @Test
    fun `apply writes the form into the walk with trimmed notes and the selected issues and concerns`() {
        behaviourIssueRepository.addBehaviourIssues("Pulling", "Barking")
        medicalConcernRepository.addMedicalConcerns("Limping", "Vomit")
        runEditorTest { editor ->
            editor.setRating(6)
            editor.setOptionalRating(OptionalRating.ENERGY_LEVEL, 2)
            editor.selectStoolPassed(true)
            editor.selectStoolTexture(StoolTexture.FIRM)
            editor.selectEquipment(Equipment.HARNESS)
            editor.onTimesChanged(start, end, autoFetch = true)
            editor.selectCondition(WeatherCondition.CLOUDY)
            editor.toggleBehaviourIssue(BehaviourIssue(2, "Barking"))
            editor.toggleMedicalConcern(MedicalConcern(1, "Limping"))
            editor.setNotes("  Off-lead in the field  ")

            assertEquals(
                walk.copy(
                    notes = "Off-lead in the field",
                    equipment = Equipment.HARNESS,
                    rating = 6,
                    optionalRatings = mapOf(OptionalRating.ENERGY_LEVEL to 2),
                    stool = Stool(passed = true, texture = StoolTexture.FIRM),
                    weather = fetched.copy(condition = WeatherCondition.CLOUDY),
                    behaviourIssues = listOf(BehaviourIssue(2, "Barking")),
                    medicalConcerns = listOf(MedicalConcern(1, "Limping")),
                ),
                editor.applyTo(walk),
            )
        }
    }

    @Test
    fun `a place that cannot be saved says so`() = runEditorTest { editor ->
        val shown = shownMessages(userMessages)
        placeRepository.failures.failWrites = true
        editor.selectPlace(Place("Uppsala", null, Coordinates(59.86, 17.64)))
        assertEquals(listOf(R.string.error_save_place), shown)
    }

    @Test
    fun `a failed add says so and selects nothing`() = runEditorTest { editor ->
        val shown = shownMessages(userMessages)
        behaviourIssueRepository.failures.failWrites = true
        medicalConcernRepository.failures.failWrites = true
        editor.addBehaviourIssue("Digging")
        editor.addMedicalConcern("Itching")
        assertTrue(editor.state.value.selectedBehaviourIssueIds.isEmpty())
        assertTrue(editor.state.value.selectedMedicalConcernIds.isEmpty())
        assertEquals(listOf(R.string.error_add_name, R.string.error_add_name), shown)
    }
}
