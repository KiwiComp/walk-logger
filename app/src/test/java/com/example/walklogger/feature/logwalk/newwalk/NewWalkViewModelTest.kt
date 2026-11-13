package com.example.walklogger.feature.logwalk.newwalk

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeHandlerRepository
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakeWalkLocationCapturer
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    private val walkLocationCapturer = FakeWalkLocationCapturer()
    private val locationRepository = FakeLocationRepository()
    private val viewModel by lazy {
        NewWalkViewModel(
            date = today,
            dogRepository = dogRepository,
            handlerRepository = handlerRepository,
            walkRepository = walkRepository,
            walkLocationCapturer = walkLocationCapturer,
            locationPermission = LocationPermissionUseCase(locationRepository),
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
    fun `the first stored handler starts selected`() = runTest {
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            assertEquals(setOf("Anna"), expectMostRecentItem().selectedHandlers)
        }
    }

    @Test
    fun `handlers toggle on and off independently`() = runTest {
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
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
            viewModel.toggleHandler("Anna")
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
    fun `collar is the default equipment and can be changed`() = runTest {
        viewModel.uiState.test {
            assertEquals(Equipment.COLLAR, expectMostRecentItem().equipment)
            viewModel.selectEquipment(Equipment.HARNESS)
            assertEquals(Equipment.HARNESS, expectMostRecentItem().equipment)
        }
    }

    @Test
    fun `starting stores the walk for the active dog and reports its id`() = runTest {
        dogRepository.addDogs(Dog(id = 3, name = "Sixten"))
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            viewModel.toggleHandler("Jonas")
            viewModel.selectEquipment(Equipment.HARNESS)
            viewModel.startWalk()

            val state = expectMostRecentItem()
            val walk = walkRepository.observeActiveWalk().first()!!
            assertEquals(walk.id, state.startedWalkId)
            assertEquals(3L, walk.dogId)
            assertEquals(listOf("Anna", "Jonas"), walk.handlers)
            assertEquals(Equipment.HARNESS, walk.equipment)
            assertFalse(state.canStart)
            assertEquals(listOf(walk.id), walkLocationCapturer.capturedWalkIds)
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

    @Test
    fun `a failed start lets the user try again and says so`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Kim")
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.startWalk()
            val state = expectMostRecentItem()
            assertFalse(state.isStarting)
            assertNull(state.startedWalkId)
            assertTrue(state.canStart)
            assertEquals(R.string.error_start_walk, viewModel.uiState.value.userMessage)
        }
        assertTrue(walkLocationCapturer.capturedWalkIds.isEmpty())
    }

    @Test
    fun `a shown message is cleared`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Kim")
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.startWalk()
            viewModel.userMessageShown(R.string.error_start_walk)
            assertNull(expectMostRecentItem().userMessage)
        }
    }

    @Test
    fun `the first start asks for location and starts the walk once answered`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Kim")
        locationRepository.isGranted = false
        viewModel.uiState.test {
            viewModel.startWalk()
            assertTrue(expectMostRecentItem().shouldRequestLocationPermission)
            assertNull(walkRepository.observeActiveWalk().first())

            locationRepository.isGranted = true
            viewModel.onLocationPermissionAnswered()
            val state = expectMostRecentItem()
            assertFalse(state.shouldRequestLocationPermission)
            val walk = walkRepository.observeActiveWalk().first()!!
            assertEquals(walk.id, state.startedWalkId)
            assertEquals(listOf(walk.id), walkLocationCapturer.capturedWalkIds)
        }
        assertTrue(locationRepository.locationPermissionRequested.first())
    }

    @Test
    fun `a denied location still starts the walk`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Kim")
        locationRepository.isGranted = false
        viewModel.uiState.test {
            viewModel.startWalk()
            expectMostRecentItem()
            viewModel.onLocationPermissionAnswered()
            assertNotNull(expectMostRecentItem().startedWalkId)
        }
    }

    @Test
    fun `does not ask again once it has asked`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Kim")
        locationRepository.isGranted = false
        locationRepository.markLocationPermissionRequested()
        viewModel.uiState.test {
            viewModel.startWalk()
            val state = expectMostRecentItem()
            assertFalse(state.shouldRequestLocationPermission)
            assertNotNull(state.startedWalkId)
        }
    }

    @Test
    fun `with location granted the walk starts without asking`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Kim")
        viewModel.uiState.test {
            viewModel.startWalk()
            val state = expectMostRecentItem()
            assertFalse(state.shouldRequestLocationPermission)
            assertNotNull(state.startedWalkId)
        }
    }
}
