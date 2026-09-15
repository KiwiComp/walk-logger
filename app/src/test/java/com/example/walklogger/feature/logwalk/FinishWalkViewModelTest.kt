package com.example.walklogger.feature.logwalk

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.Wind
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

class FinishWalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 9, 12)
    private val zone = ZoneOffset.ofHours(2)
    private val now = date.atTime(7, 39)
    private val clock = Clock.fixed(now.toInstant(zone), zone)
    private val dogRepository = FakeDogRepository()
    private val walkRepository = FakeWalkRepository()
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
        FinishWalkViewModel(walkId = 1, dogRepository = dogRepository, walkRepository = walkRepository, clock = clock)
    }

    @Test
    fun `prefills the recorded times, handlers and note`() = runTest {
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
            assertEquals(Equipment.HARNESS, state.equipment)
            assertEquals("Barked", state.notes)
            assertEquals(SaveBlocker.RATING, state.saveBlocker)
        }
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
            viewModel.setRating(6)
            viewModel.setEndTime(LocalTime.of(7, 0))
            assertEquals(SaveBlocker.END_BEFORE_START, expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `an end in the future blocks saving`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setRating(6)
            viewModel.setEndTime(LocalTime.of(8, 0))
            assertEquals(SaveBlocker.IN_FUTURE, expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `saving updates the stored walk and keeps its id, dog and handlers`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setStartTime(LocalTime.of(7, 0))
            viewModel.setRating(6)
            viewModel.selectEquipment(Equipment.COLLAR)
            viewModel.selectRain(Rain.NONE)
            viewModel.selectTemperature(Temperature.COOL)
            viewModel.selectWind(Wind.BREEZE)
            viewModel.setNotes("  Off-lead in the field  ")
            viewModel.save()

            assertTrue(expectMostRecentItem().isSaved)
            val stored = walkRepository.observeWalk(1).first()!!
            assertEquals(1L, stored.id)
            assertEquals(1L, stored.dogId)
            assertEquals(listOf("Anna"), stored.handlers)
            assertEquals(date.atTime(7, 0).toInstant(zone).toEpochMilli(), stored.startTime)
            assertEquals(walk.endTime, stored.endTime)
            assertEquals(6, stored.rating)
            assertEquals(Equipment.COLLAR, stored.equipment)
            assertEquals(Rain.NONE, stored.rain)
            assertEquals(Temperature.COOL, stored.temperature)
            assertEquals(Wind.BREEZE, stored.wind)
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
}
