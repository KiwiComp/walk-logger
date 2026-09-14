package com.example.walklogger.feature.logwalk

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeHandlerRepository
import com.example.walklogger.testing.MainDispatcherRule
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewWalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 12)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val dogRepository = FakeDogRepository()
    private val handlerRepository = FakeHandlerRepository()
    private var date = today
    private val viewModel by lazy {
        NewWalkViewModel(
            date = date,
            dogRepository = dogRepository,
            handlerRepository = handlerRepository,
            clock = clock,
        )
    }

    @Test
    fun `today's date allows starting a timed walk`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(today, state.date)
            assertTrue(state.isToday)
        }
    }

    @Test
    fun `a past date cannot start a timed walk`() = runTest {
        date = today.minusDays(1)
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(today.minusDays(1), state.date)
            assertFalse(state.isToday)
        }
    }

    @Test
    fun `shows the active dog's name`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        viewModel.uiState.test {
            assertEquals("Sixten", expectMostRecentItem().dogName)
        }
    }

    @Test
    fun `dog name is null without a registered dog`() = runTest {
        viewModel.uiState.test {
            assertNull(expectMostRecentItem().dogName)
        }
    }

    @Test
    fun `lists stored handlers`() = runTest {
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            assertEquals(listOf("Anna", "Jonas"), expectMostRecentItem().handlers)
        }
    }

    @Test
    fun `handlers toggle on and off independently`() = runTest {
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            viewModel.toggleHandler("Anna")
            viewModel.toggleHandler("Jonas")
            assertEquals(setOf("Anna", "Jonas"), expectMostRecentItem().selectedHandlers)
            viewModel.toggleHandler("Anna")
            assertEquals(setOf("Jonas"), expectMostRecentItem().selectedHandlers)
        }
    }

    @Test
    fun `cannot start until a handler is selected`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().canStart)
            viewModel.toggleHandler("Anna")
            assertTrue(expectMostRecentItem().canStart)
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
    fun `adding an existing handler selects it without duplicating`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            viewModel.addHandler("anna")
            val state = expectMostRecentItem()
            assertEquals(listOf("Anna"), state.handlers)
            assertEquals(setOf("Anna"), state.selectedHandlers)
        }
    }

    @Test
    fun `harness is the default equipment and can be changed`() = runTest {
        viewModel.uiState.test {
            assertEquals(Equipment.HARNESS, expectMostRecentItem().equipment)
            viewModel.selectEquipment(Equipment.COLLAR)
            assertEquals(Equipment.COLLAR, expectMostRecentItem().equipment)
        }
    }
}
