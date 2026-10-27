package com.example.walklogger.feature.walkdetail

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeWalkRepository
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


class WalkDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneOffset.ofHours(2)
    private val date = LocalDate.of(2026, 9, 15)
    private val clock = Clock.fixed(date.atTime(20, 0).toInstant(zone), zone)
    private val dogRepository = FakeDogRepository()
    private val walkRepository = FakeWalkRepository()
    private val userMessages = UserMessages()
    private val viewModel by lazy {
        WalkDetailViewModel(
            walkId = 1,
            walkRepository = walkRepository,
            dogRepository = dogRepository,
            userMessages = userMessages,
            clock = clock,
        )
    }

    private val issue = BehaviourIssue(1, "Pulling", key = "pulling")
    private val concern = MedicalConcern(4, "Limping", key = "limping")

    private fun seedWalk(walk: Walk = completeWalk()) {
        dogRepository.addDogs(Dog(id = 3, name = "Sixten"))
        walkRepository.addWalks(walk)
    }

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
        rain = Rain.LIGHT,
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
            assertEquals(Rain.LIGHT, state.rain)
            assertTrue(state.hasWeather)
            assertEquals(listOf(issue), state.behaviourIssues)
            assertEquals(listOf(concern), state.medicalConcerns)
            assertEquals("Windy", state.notes)
            assertTrue(state.canDiscard)
        }
    }

    @Test
    fun `weather counts as recorded only when some condition is set`() = runTest {
        seedWalk(completeWalk().copy(rain = null))
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().hasWeather)
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
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.discard()
            val state = expectMostRecentItem()
            assertFalse(state.isDiscarding)
            assertFalse(state.isDiscarded)
            assertTrue(state.canDiscard)
        }
        assertEquals(listOf(R.string.error_delete_walk), shown)
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
