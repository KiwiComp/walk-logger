package com.example.walklogger.feature.logwalk.finishwalk

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.domain.SaveWalkUseCase
import com.example.walklogger.feature.logwalk.SaveBlocker
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeBehaviourIssueRepository
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakeMedicalConcernRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.FakeWeatherRepository
import com.example.walklogger.testing.MainDispatcherRule
import com.example.walklogger.testing.shownMessages
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
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
    private val userMessages = UserMessages()
    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val activeWalkLocationRepository = FakeActiveWalkLocationRepository()
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
            behaviourIssueRepository = behaviourIssueRepository,
            medicalConcernRepository = medicalConcernRepository,
            walkRepository = walkRepository,
            userMessages = userMessages,
            clock = clock,
            fetchWalkWeather = FetchWalkWeatherUseCase(
                locationRepository,
                weatherRepository,
                activeWalkLocationRepository,
            ),
            locationPermission = LocationPermissionUseCase(locationRepository),
            locationRepository = locationRepository,
            saveWalk = SaveWalkUseCase(walkRepository, activeWalkLocationRepository),
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
            assertEquals(date, state.date)
            assertEquals(LocalTime.of(7, 5), state.startTime)
            assertEquals(LocalTime.of(7, 39), state.endTime)
            assertEquals(34, state.durationMinutes)
            assertEquals(listOf("Anna"), state.handlers)
            assertEquals(Equipment.HARNESS, state.form.equipment)
            assertEquals("Barked", state.form.notes)
            assertEquals(SaveBlocker.RATING, state.saveBlocker)
            assertNull(state.form.stool)
        }
    }

    @Test
    fun `fetches the weather for the walk when it opens, and again when its times change`() =
        runTest {
            walkRepository.addWalks(walk)
            viewModel.uiState.test {
                assertEquals(weatherRepository.weather, expectMostRecentItem().form.fetchedWeather)
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
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            viewModel.formActions.setRating(4)
            walkRepository.failures.failWrites = true
            viewModel.save()
            val state = expectMostRecentItem()
            assertFalse(state.isSaving)
            assertFalse(state.isSaved)
            assertTrue(state.canSave)
        }
        assertEquals(listOf(R.string.error_save_walk), shown)
    }

    @Test
    fun `a missing walk closes the screen and says so`() = runTest {
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().isNotFound)
        }
        assertEquals(listOf(R.string.error_walk_not_found), shown)
    }
}
