package com.example.walklogger.feature.logwalk.pastwalk

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.Wind
import com.example.walklogger.feature.logwalk.MAX_NOTES_LENGTH
import com.example.walklogger.feature.logwalk.SaveBlocker
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
    private val now = date.atTime(20, 0)
    private val clock = Clock.fixed(now.toInstant(zone), zone)
    private val dogRepository = FakeDogRepository()
    private val handlerRepository = FakeHandlerRepository()
    private val walkRepository = FakeWalkRepository()
    private var walkId: Long? = null
    private val viewModel by lazy {
        LogPastWalkViewModel(
            date = date,
            walkId = walkId,
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
    fun `weather starts collapsed for a new walk and toggles`() = runTest {
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().isWeatherExpanded)
            viewModel.toggleWeather()
            assertTrue(expectMostRecentItem().isWeatherExpanded)
            viewModel.toggleWeather()
            assertFalse(expectMostRecentItem().isWeatherExpanded)
        }
    }

    @Test
    fun `clear weather resets every condition`() = runTest {
        viewModel.uiState.test {
            viewModel.selectRain(Rain.LIGHT)
            viewModel.selectTemperature(Temperature.COOL)
            viewModel.selectWind(Wind.BREEZE)
            viewModel.selectSnow(Snow.HEAVY)
            viewModel.clearWeather()
            val state = expectMostRecentItem()
            assertNull(state.rain)
            assertNull(state.temperature)
            assertNull(state.wind)
            assertNull(state.snow)
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
    fun `date picker is capped at today`() = runTest {
        viewModel.uiState.test {
            assertEquals(date, expectMostRecentItem().maxDate)
        }
    }

    @Test
    fun `a walk ending after now is blocked`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            fillInValidWalk()
            viewModel.setEndTime(now.toLocalTime().plusMinutes(1))
            assertEquals(SaveBlocker.IN_FUTURE, expectMostRecentItem().saveBlocker)
            viewModel.setEndTime(now.toLocalTime())
            assertNull(expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `a walk on a future day is blocked`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            fillInValidWalk()
            viewModel.setDate(date.plusDays(1))
            assertEquals(SaveBlocker.IN_FUTURE, expectMostRecentItem().saveBlocker)
            viewModel.setDate(date.minusDays(1))
            assertNull(expectMostRecentItem().saveBlocker)
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
            viewModel.selectSnow(Snow.NONE)
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
        assertEquals(Snow.NONE, walk.snow)
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
            viewModel.setNotes("x".repeat(MAX_NOTES_LENGTH + 1))
            assertEquals(MAX_NOTES_LENGTH, expectMostRecentItem().notes.length)
        }
    }

    private fun seedLoggedWalk(): Walk {
        val walk = Walk(
            id = 42,
            dogId = 7,
            handlers = listOf("Jonas"),
            startTime = date.minusDays(2).atTime(8, 15).toInstant(zone).toEpochMilli(),
            endTime = date.minusDays(2).atTime(8, 50).toInstant(zone).toEpochMilli(),
            notes = "Muddy",
            equipment = Equipment.COLLAR,
            rating = 3,
            rain = Rain.HEAVY,
            temperature = Temperature.COOL,
            wind = Wind.BREEZE,
            snow = Snow.LIGHT,
        )
        walkRepository.addWalks(walk)
        walkId = walk.id
        return walk
    }

    @Test
    fun `editing prefills the form from the stored walk`() = runTest {
        seedLoggedWalk()
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state.isEditing)
            assertFalse(state.isLoading)
            assertEquals(date.minusDays(2), state.date)
            assertEquals(LocalTime.of(8, 15), state.startTime)
            assertEquals(LocalTime.of(8, 50), state.endTime)
            assertEquals(setOf("Jonas"), state.selectedHandlers)
            assertEquals(3, state.rating)
            assertEquals(Equipment.COLLAR, state.equipment)
            assertEquals(Rain.HEAVY, state.rain)
            assertEquals(Temperature.COOL, state.temperature)
            assertEquals(Wind.BREEZE, state.wind)
            assertEquals(Snow.LIGHT, state.snow)
            assertTrue(state.isWeatherExpanded)
            assertEquals("Muddy", state.notes)
            assertTrue(state.canSave)
        }
    }

    @Test
    fun `saving an edit updates the walk and keeps its id and dog`() = runTest {
        val original = seedLoggedWalk()
        dogRepository.addDogs(Dog(id = 1, name = "Other"))
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            expectMostRecentItem()
            viewModel.setRating(6)
            viewModel.setNotes("Dry after all")
            viewModel.toggleHandler("Anna")
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        val walk = walkRepository.observeWalks().first().single()
        assertEquals(original.id, walk.id)
        assertEquals(original.dogId, walk.dogId)
        assertEquals(original.startTime, walk.startTime)
        assertEquals(6, walk.rating)
        assertEquals("Dry after all", walk.notes)
        assertEquals(listOf("Anna", "Jonas"), walk.handlers)
    }

    @Test
    fun `delete removes the edited walk and flags completion`() = runTest {
        seedLoggedWalk()
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().canDelete)
            viewModel.delete()
            val state = expectMostRecentItem()
            assertTrue(state.isDeleted)
            assertFalse(state.canSave)
        }
        assertTrue(walkRepository.observeWalks().first().isEmpty())
    }

    @Test
    fun `delete is not offered when logging a new walk`() = runTest {
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().canDelete)
            viewModel.delete()
            assertFalse(viewModel.uiState.value.isDeleted)
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
