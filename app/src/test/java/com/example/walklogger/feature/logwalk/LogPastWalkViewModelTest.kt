package com.example.walklogger.feature.logwalk

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeHandlerRepository
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

class LogPastWalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 9, 12)
    private val zone = ZoneOffset.ofHours(2)
    private val clock = Clock.fixed(date.atStartOfDay().toInstant(zone), zone)
    private val dogRepository = FakeDogRepository()
    private val handlerRepository = FakeHandlerRepository()
    private val walkRepository = FakeWalkRepository()
    private val viewModel by lazy {
        LogPastWalkViewModel(
            date = date,
            dogRepository = dogRepository,
            handlerRepository = handlerRepository,
            walkRepository = walkRepository,
            clock = clock,
        )
    }

    private fun fillInValidWalk() {
        viewModel.toggleHandler("Anna")
        viewModel.setStartTime(LocalTime.of(17, 40))
        viewModel.setEndTime(LocalTime.of(18, 5))
        viewModel.setRating(5)
    }

    @Test
    fun `date is prefilled and times are empty`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(date, state.date)
            assertNull(state.startTime)
            assertNull(state.endTime)
            assertNull(state.durationMinutes)
        }
    }

    @Test
    fun `shows the active dog and stored handlers`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Sixten", state.dogName)
            assertEquals(listOf("Anna", "Jonas"), state.handlers)
        }
    }

    @Test
    fun `duration follows the entered times`() = runTest {
        viewModel.uiState.test {
            viewModel.setStartTime(LocalTime.of(17, 40))
            viewModel.setEndTime(LocalTime.of(18, 5))
            assertEquals(25, expectMostRecentItem().durationMinutes)
        }
    }

    @Test
    fun `save is blocked by the first missing input in form order`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            assertEquals(SaveBlocker.HANDLER, expectMostRecentItem().saveBlocker)
            viewModel.toggleHandler("Anna")
            assertEquals(SaveBlocker.TIMES, expectMostRecentItem().saveBlocker)
            viewModel.setStartTime(LocalTime.of(18, 5))
            viewModel.setEndTime(LocalTime.of(17, 40))
            assertEquals(SaveBlocker.END_BEFORE_START, expectMostRecentItem().saveBlocker)
            viewModel.setEndTime(LocalTime.of(18, 30))
            assertEquals(SaveBlocker.RATING, expectMostRecentItem().saveBlocker)
            viewModel.setRating(4)
            val state = expectMostRecentItem()
            assertNull(state.saveBlocker)
            assertTrue(state.canSave)
        }
    }

    @Test
    fun `adding a handler stores and selects it`() = runTest {
        viewModel.uiState.test {
            viewModel.addHandler("  Elin ")
            val state = expectMostRecentItem()
            assertEquals(listOf("Elin"), state.handlers)
            assertEquals(setOf("Elin"), state.selectedHandlers)
        }
    }

    @Test
    fun `save stores the walk for the active dog and flags completion`() = runTest {
        dogRepository.addDogs(Dog(id = 7, name = "Sixten"))
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            fillInValidWalk()
            viewModel.selectEquipment(Equipment.COLLAR)
            viewModel.selectRain(Rain.LIGHT)
            viewModel.selectTemperature(Temperature.COLD)
            viewModel.selectWind(Wind.WINDY)
            viewModel.setNotes(" Pulled at the gate ")
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        val walk = walkRepository.observeWalks().first().single()
        assertEquals(7, walk.dogId)
        assertEquals(listOf("Anna"), walk.handlers)
        assertEquals(date.atTime(17, 40).toInstant(zone).toEpochMilli(), walk.startTime)
        assertEquals(date.atTime(18, 5).toInstant(zone).toEpochMilli(), walk.endTime)
        assertEquals(5, walk.rating)
        assertEquals(Equipment.COLLAR, walk.equipment)
        assertEquals(Rain.LIGHT, walk.rain)
        assertEquals(Temperature.COLD, walk.temperature)
        assertEquals(Wind.WINDY, walk.wind)
        assertEquals("Pulled at the gate", walk.notes)
    }

    @Test
    fun `save uses the date picked in the form`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Anna")
        val picked = date.minusDays(3)
        viewModel.uiState.test {
            fillInValidWalk()
            viewModel.setDate(picked)
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        val walk = walkRepository.observeWalks().first().single()
        assertEquals(picked.atTime(17, 40).toInstant(zone).toEpochMilli(), walk.startTime)
    }

    @Test
    fun `notes are cut at the maximum length`() = runTest {
        viewModel.uiState.test {
            viewModel.setNotes("x".repeat(LogPastWalkViewModel.MAX_NOTES_LENGTH + 1))
            assertEquals(LogPastWalkViewModel.MAX_NOTES_LENGTH, expectMostRecentItem().notes.length)
        }
    }

    @Test
    fun `save does nothing while the form is incomplete`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        viewModel.uiState.test {
            viewModel.save()
            assertFalse(expectMostRecentItem().isSaved)
        }
        assertTrue(walkRepository.observeWalks().first().isEmpty())
    }
}
