package com.example.walklogger.feature.walkdetail

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.domain.DiscardWalkUseCase
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
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

class WalkDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneOffset.ofHours(2)
    private val date = LocalDate.of(2026, 9, 15)
    private val clock = Clock.fixed(date.atTime(20, 0).toInstant(zone), zone)
    private val dogRepository = FakeDogRepository()
    private val walkRepository = FakeWalkRepository()
    private val viewModel by lazy {
        WalkDetailViewModel(
            walkId = 1,
            walkRepository = walkRepository,
            dogRepository = dogRepository,
            clock = clock,
            discardWalk = DiscardWalkUseCase(walkRepository, FakeActiveWalkLocationRepository()),
        )
    }

    private val issue = BehaviourIssue(1, "Pulling", key = "pulling")
    private val concern = MedicalConcern(4, "Limping", key = "limping")

    private fun seedWalk(walk: Walk = completeWalk()) {
        dogRepository.addDogs(Dog(id = 3, name = "Sixten"))
        walkRepository.addWalks(walk)
    }

    private val weather =
        WalkWeather(condition = WeatherCondition.LIGHT_RAIN, temperatureCelsius = 6.5, rainMm = 0.4)

    private fun completeWalk() = Walk(
        id = 1,
        dogId = 3,
        handlers = listOf("Kim", "Anna"),
        startTime = date.atTime(7, 12).toInstant(zone).toEpochMilli(),
        endTime = date.atTime(7, 46).toInstant(zone).toEpochMilli(),
        notes = "Windy",
        equipment = Equipment.COLLAR,
        rating = 5,
        optionalRatings = mapOf(OptionalRating.REACTIVITY to 6),
        stool = Stool(passed = true, colour = StoolColour.RED_STREAKED),
        weather = weather,
        behaviourIssues = listOf(issue),
        medicalConcerns = listOf(concern),
    )

    @Test
    fun `starts loading and empty`() = runTest {
        seedWalk()
        val state = WalkDetailUiState()
        assertTrue(state.isLoading)
        assertFalse(state.canDiscard)
        assertNull(state.durationMinutes)
    }

    @Test
    fun `shows the stored walk in the clock's zone with its dog's name`() = runTest {
        seedWalk()
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(date, state.date)
            assertEquals(LocalTime.of(7, 12), state.startTime)
            assertEquals(LocalTime.of(7, 46), state.endTime)
            assertEquals(34, state.durationMinutes)
            assertEquals("Sixten", state.dogName)
            assertEquals(listOf("Kim", "Anna"), state.handlers)
            assertEquals(Equipment.COLLAR, state.equipment)
            assertEquals(5, state.rating)
            assertEquals(mapOf(OptionalRating.REACTIVITY to 6), state.optionalRatings)
            assertEquals(Stool(passed = true, colour = StoolColour.RED_STREAKED), state.stool)
            assertEquals(weather, state.weather)
            assertEquals(listOf(issue), state.behaviourIssues)
            assertEquals(listOf(concern), state.medicalConcerns)
            assertEquals("Windy", state.notes)
            assertTrue(state.canDiscard)
        }
    }

    @Test
    fun `a walk without weather shows none`() = runTest {
        seedWalk(completeWalk().copy(weather = null))
        viewModel.uiState.test {
            assertNull(expectMostRecentItem().weather)
        }
    }

    @Test
    fun `reflects an edit made while the screen is open`() = runTest {
        seedWalk()
        viewModel.uiState.test {
            assertEquals(5, expectMostRecentItem().rating)
            walkRepository.updateWalk(completeWalk().copy(rating = 7, notes = "Better"))
            val state = expectMostRecentItem()
            assertEquals(7, state.rating)
            assertEquals("Better", state.notes)
        }
    }

    @Test
    fun `discard removes the walk and flags completion`() = runTest {
        seedWalk()
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().canDiscard)
            viewModel.discard()
            val state = expectMostRecentItem()
            assertTrue(state.isDiscarded)
            assertFalse(state.canDiscard)
        }
        assertTrue(walkRepository.observeWalks().first().isEmpty())
    }

    @Test
    fun `discard does nothing before the walk has loaded`() = runTest {
        viewModel.discard()
        assertFalse(viewModel.uiState.value.isDiscarded)
    }

    @Test
    fun `a failed discard keeps the walk and says so`() = runTest {
        seedWalk()
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.discard()
            val state = expectMostRecentItem()
            assertFalse(state.isDiscarding)
            assertFalse(state.isDiscarded)
            assertTrue(state.canDiscard)
            assertEquals(R.string.error_delete_walk, state.userMessage)
        }
    }

    @Test
    fun `a shown message is cleared`() = runTest {
        seedWalk()
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.discard()
            viewModel.userMessageShown(R.string.error_delete_walk)
            assertNull(expectMostRecentItem().userMessage)
        }
    }

    @Test
    fun `a missing walk closes the screen`() = runTest {
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().isNotFound)
        }
    }

    @Test
    fun `a deleted dog's walk keeps its name and is read-only`() = runTest {
        dogRepository.addDogs(Dog(id = 3, name = "Sixten", isDeleted = true))
        walkRepository.addWalks(completeWalk())
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Sixten", state.dogName)
            assertTrue(state.isDogDeleted)
            assertFalse(state.canEdit)
            assertFalse(state.canDiscard)
        }
    }
}
