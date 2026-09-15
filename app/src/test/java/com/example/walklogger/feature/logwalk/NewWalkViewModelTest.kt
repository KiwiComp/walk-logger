package com.example.walklogger.feature.logwalk

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeHandlerRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.flow.first
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
    private val dogRepository = FakeDogRepository()
    private val handlerRepository = FakeHandlerRepository()
    private val walkRepository = FakeWalkRepository()
    private val viewModel by lazy {
        NewWalkViewModel(
            date = today,
            dogRepository = dogRepository,
            handlerRepository = handlerRepository,
            walkRepository = walkRepository,
        )
    }

    @Test
    fun `shows the chosen date`() = runTest {
        viewModel.uiState.test {
            assertEquals(today, expectMostRecentItem().date)
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

    @Test
    fun `starting stores the walk for the active dog and reports its id`() = runTest {
        dogRepository.addDogs(Dog(id = 3, name = "Sixten"))
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            viewModel.toggleHandler("Jonas")
            viewModel.selectEquipment(Equipment.COLLAR)
            viewModel.startWalk()

            val state = expectMostRecentItem()
            val walk = walkRepository.observeActiveWalk().first()!!
            assertEquals(walk.id, state.startedWalkId)
            assertEquals(3L, walk.dogId)
            assertEquals(listOf("Jonas"), walk.handlers)
            assertEquals(Equipment.COLLAR, walk.equipment)
            assertFalse(state.canStart)
        }
    }

    @Test
    fun `starting without a handler does nothing`() = runTest {
        dogRepository.addDogs(Dog(id = 3, name = "Sixten"))
        viewModel.uiState.test {
            viewModel.startWalk()
            assertNull(expectMostRecentItem().startedWalkId)
            assertNull(walkRepository.observeActiveWalk().first())
        }
    }
}
