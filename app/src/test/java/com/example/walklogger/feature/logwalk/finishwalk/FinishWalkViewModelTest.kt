package com.example.walklogger.feature.logwalk.finishwalk

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.RoutePoint
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.domain.DiscardWalkUseCase
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.domain.SaveWalkUseCase
import com.example.walklogger.feature.logwalk.SaveBlocker
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeBehaviourIssueRepository
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeErrorReporter
import com.example.walklogger.testing.FakeHandlerRepository
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakeMedicalConcernRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.FakeWalkRouteRepository
import com.example.walklogger.testing.FakeWeatherRepository
import com.example.walklogger.testing.MainDispatcherRule
import com.example.walklogger.testing.walkFormEditorFactory
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FinishWalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 9, 12)
    private val zone = ZoneOffset.ofHours(2)
    private val now = date.atTime(7, 39)
    private val clock = Clock.fixed(now.toInstant(zone), zone)
    private val dogRepository = FakeDogRepository()
    private val behaviourIssueRepository = FakeBehaviourIssueRepository()
    private val medicalConcernRepository = FakeMedicalConcernRepository()
    private val walkRepository = FakeWalkRepository()
    private val handlerRepository = FakeHandlerRepository().apply { addHandlers("Anna", "Jonas") }
    private val errorReporter = FakeErrorReporter()
    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val activeWalkLocationRepository = FakeActiveWalkLocationRepository()
    private val walkRouteRepository = FakeWalkRouteRepository()
    private val walk = Walk(
        id = 1,
        dogId = 1,
        handlers = listOf("Anna"),
        startTime = date.atTime(7, 5).toInstant(zone).toEpochMilli(),
        endTime = now.toInstant(zone).toEpochMilli(),
        equipment = Equipment.HARNESS,
        notes = "Barked",
    )
    private val viewModel by lazy {
        FinishWalkViewModel(
            walkId = 1,
            dogRepository = dogRepository,
            handlerRepository = handlerRepository,
            walkRepository = walkRepository,
            walkRouteRepository = walkRouteRepository,
            clock = clock,
            formFactory = walkFormEditorFactory(
                behaviourIssueRepository = behaviourIssueRepository,
                medicalConcernRepository = medicalConcernRepository,
                errorReporter = errorReporter,
                fetchWalkWeather = FetchWalkWeatherUseCase(
                    locationRepository,
                    weatherRepository,
                    activeWalkLocationRepository,
                    errorReporter,
                ),
                locationPermission = LocationPermissionUseCase(locationRepository),
                locationRepository = locationRepository,
            ),
            saveWalk = SaveWalkUseCase(walkRepository, activeWalkLocationRepository, errorReporter),
            discardWalk = DiscardWalkUseCase(
                walkRepository,
                activeWalkLocationRepository,
                errorReporter,
            ),
            errorReporter = errorReporter,
        )
    }

    @Test
    fun `prefills the recorded times, handlers and form`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals("Sixten", state.dogName)
            assertEquals(date, state.times.date)
            assertEquals(LocalTime.of(7, 5), state.times.startTime)
            assertEquals(LocalTime.of(7, 39), state.times.endTime)
            assertEquals(34, state.times.durationMinutes)
            assertEquals(setOf("Anna"), state.selectedHandlers)
            assertEquals(Equipment.HARNESS, state.form.equipment)
            assertEquals("Barked", state.form.notes)
            assertEquals(SaveBlocker.RATING, state.saveBlocker)
            assertNull(state.form.stool)
        }
    }

    @Test
    fun `shows the route within the edited times, and the hidden part again once lengthened`() =
        runTest {
            walkRepository.addWalks(walk)
            val early = pointAt(LocalTime.of(7, 10), Coordinates(59.3300, 18.0600))
            val late = pointAt(LocalTime.of(7, 35), Coordinates(59.3350, 18.0700))
            walkRouteRepository.addPoint(walk.id, early)
            walkRouteRepository.addPoint(walk.id, late)
            viewModel.uiState.test {
                val whole = listOf(early.coordinates, late.coordinates)
                assertEquals(whole, expectMostRecentItem().route)
                viewModel.setEndTime(LocalTime.of(7, 30))
                assertEquals(listOf(early.coordinates), expectMostRecentItem().route)
                viewModel.setEndTime(LocalTime.of(7, 39))
                assertEquals(whole, expectMostRecentItem().route)
            }
        }

    @Test
    fun `fetches the weather for the walk when it opens, and again when its times change`() =
        runTest {
            walkRepository.addWalks(walk)
            viewModel.uiState.test {
                assertEquals(
                    weatherRepository.weather,
                    expectMostRecentItem().form.weather.fetchedWeather,
                )
                viewModel.setStartTime(LocalTime.of(7, 0))
                expectMostRecentItem()
            }
            assertEquals(2, weatherRepository.requests.size)
        }

    @Test
    fun `saving stores the fetched weather`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            expectMostRecentItem()
            viewModel.formActions.setRating(5)
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        assertEquals(
            weatherRepository.weather,
            walkRepository.observeWalks().first().single().weather,
        )
    }

    @Test
    fun `saving while the weather loads waits for it and stores it`() = runTest {
        weatherRepository.delayMillis = 3_000
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            expectMostRecentItem()
            viewModel.formActions.setRating(5)
            viewModel.save()
            advanceUntilIdle()
            assertTrue(expectMostRecentItem().isSaved)
        }
        assertEquals(
            weatherRepository.weather,
            walkRepository.observeWalks().first().single().weather,
        )
    }

    @Test
    fun `saving gives up waiting for the weather after the limit`() = runTest {
        weatherRepository.delayMillis = 10_000
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            expectMostRecentItem()
            viewModel.formActions.setRating(5)
            viewModel.save()
            advanceTimeBy(5_001)
            assertTrue(expectMostRecentItem().isSaved)
        }
        assertNull(walkRepository.observeWalks().first().single().weather)
    }

    @Test
    fun `fetches for the position stored at the start and clears it once saved`() = runTest {
        val start = Coordinates(59.86, 17.64)
        activeWalkLocationRepository.walkLocation.value = WalkLocation(walkId = 1, start)
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            expectMostRecentItem()
            viewModel.formActions.setRating(5)
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        assertEquals(start, weatherRepository.requests.single().first)
        assertNull(activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `edit times toggles the pickers`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().isEditingTimes)
            viewModel.toggleEditTimes()
            assertTrue(expectMostRecentItem().isEditingTimes)
            viewModel.toggleEditTimes()
            assertFalse(expectMostRecentItem().isEditingTimes)
        }
    }

    @Test
    fun `end before start blocks saving`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.formActions.setRating(6)
            viewModel.setEndTime(LocalTime.of(7, 0))
            assertEquals(SaveBlocker.END_BEFORE_START, expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `an end in the future blocks saving`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.formActions.setRating(6)
            viewModel.setEndTime(LocalTime.of(8, 0))
            assertEquals(SaveBlocker.IN_FUTURE, expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `saving updates the stored walk and keeps its id, dog and handlers`() = runTest {
        walkRepository.addWalks(walk)
        behaviourIssueRepository.addBehaviourIssues("Pulling", "Barking")
        viewModel.uiState.test {
            viewModel.setStartTime(LocalTime.of(7, 0))
            viewModel.formActions.setRating(6)
            viewModel.formActions.selectStoolPassed(true)
            viewModel.formActions.selectStoolTexture(StoolTexture.FIRM)
            viewModel.formActions.toggleBehaviourIssue(BehaviourIssue(2, "Barking"))
            viewModel.formActions.setNotes("  Off-lead in the field  ")
            viewModel.save()

            assertTrue(expectMostRecentItem().isSaved)
            val stored = walkRepository.observeWalk(1).first()!!
            assertEquals(1L, stored.id)
            assertEquals(1L, stored.dogId)
            assertEquals(listOf("Anna"), stored.handlers)
            assertEquals(date.atTime(7, 0).toInstant(zone).toEpochMilli(), stored.startTime)
            assertEquals(walk.endTime, stored.endTime)
            assertEquals(6, stored.rating)
            assertEquals(Equipment.HARNESS, stored.equipment)
            assertEquals(Stool(passed = true, texture = StoolTexture.FIRM), stored.stool)
            assertEquals(listOf(BehaviourIssue(2, "Barking")), stored.behaviourIssues)
            assertEquals("Off-lead in the field", stored.notes)
        }
    }

    @Test
    fun `a changed handler selection is saved`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.toggleHandler("Anna")
            viewModel.toggleHandler("Jonas")
            viewModel.formActions.setRating(5)
            viewModel.save()

            assertTrue(expectMostRecentItem().isSaved)
            assertEquals(listOf("Jonas"), walkRepository.observeWalk(1).first()!!.handlers)
        }
    }

    @Test
    fun `no handler blocks saving`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.formActions.setRating(5)
            viewModel.toggleHandler("Anna")
            val state = expectMostRecentItem()
            assertEquals(SaveBlocker.HANDLER, state.saveBlocker)
            assertFalse(state.canSave)
        }
    }

    @Test
    fun `discarding deletes the walk and closes the screen`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.discard()
            assertTrue(expectMostRecentItem().isDiscarded)
            assertNull(walkRepository.observeWalk(1).first())
        }
    }

    @Test
    fun `saving without a rating does nothing`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.save()
            assertFalse(expectMostRecentItem().isSaved)
            assertNull(walkRepository.observeWalk(1).first()!!.rating)
        }
    }

    @Test
    fun `a failed save keeps the form open and says so`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.formActions.setRating(4)
            walkRepository.failures.failWrites = true
            viewModel.save()
            val state = expectMostRecentItem()
            assertFalse(state.isSaving)
            assertFalse(state.isSaved)
            assertTrue(state.canSave)
            assertEquals(R.string.error_save_walk, viewModel.uiState.value.userMessage)
            assertEquals(listOf("Failed to save walk"), errorReporter.reported.map { it.second })
        }
    }

    @Test
    fun `a missing walk closes the screen`() = runTest {
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().isNotFound)
        }
    }

    private fun pointAt(time: LocalTime, coordinates: Coordinates) =
        RoutePoint(date.atTime(time).toInstant(zone).toEpochMilli(), coordinates)
}
