package com.example.walklogger.feature.dog.registerdog

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Walk
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeErrorReporter
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

class RegisterDogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeDogRepository()
    private val walkRepository = FakeWalkRepository()
    private val errorReporter = FakeErrorReporter()
    private val viewModel by lazy { editViewModel(dogId = null) }

    private fun editViewModel(dogId: Long?) = RegisterDogViewModel(
        dogId = dogId,
        dogRepository = repository,
        walkRepository = walkRepository,
        errorReporter = errorReporter,
    )

    private fun ongoingWalk(dogId: Long) = Walk(
        id = 1,
        dogId = dogId,
        handlers = listOf("Kim"),
        startTime = 0,
        endTime = null,
        equipment = Equipment.HARNESS,
    )

    @Test
    fun `cannot save without a name`() = runTest {
        assertFalse(viewModel.uiState.value.canSave)
        viewModel.setName("   ")
        assertFalse(viewModel.uiState.value.canSave)
        viewModel.setName("Sixten")
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun `weight accepts comma and dot decimals and rejects junk`() = runTest {
        viewModel.setName("Sixten")
        viewModel.setWeight("18,5")
        assertEquals(18.5, viewModel.uiState.value.weightKg!!, 0.0)
        viewModel.setWeight("18.5")
        assertEquals(18.5, viewModel.uiState.value.weightKg!!, 0.0)
        viewModel.setWeight("heavy")
        assertNull(viewModel.uiState.value.weightKg)
        assertTrue(viewModel.uiState.value.isWeightInvalid)
        assertFalse(viewModel.uiState.value.canSave)
        viewModel.setWeight("0")
        assertTrue(viewModel.uiState.value.isWeightInvalid)
        viewModel.setWeight("")
        assertFalse(viewModel.uiState.value.isWeightInvalid)
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun `save registers the dog and flags completion`() = runTest {
        val birthDate = LocalDate.of(2023, 9, 2)
        viewModel.setName(" Sixten ")
        viewModel.setBreed("Border collie")
        viewModel.setBirthDate(birthDate)
        viewModel.setWeight("18")
        viewModel.uiState.test {
            assertFalse(awaitItem().isSaved)
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        assertEquals(
            listOf(
                Dog(
                    id = 1,
                    name = "Sixten",
                    breed = "Border collie",
                    birthDate = birthDate,
                    weightKg = 18.0,
                ),
            ),
            repository.observeDogs().first(),
        )
    }

    @Test
    fun `save without a name does nothing`() = runTest {
        viewModel.save()
        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals(emptyList<Dog>(), repository.observeDogs().first())
    }

    @Test
    fun `editing prefills the form from the stored dog`() = runTest {
        val birthDate = LocalDate.of(2023, 9, 2)
        repository.addDogs(
            Dog(
                id = 7,
                name = "Sixten",
                breed = "Border collie",
                birthDate = birthDate,
                weightKg = 18.0,
            ),
        )
        editViewModel(7).uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Sixten", state.name)
            assertEquals("Border collie", state.breed)
            assertEquals(birthDate, state.birthDate)
            assertEquals("18", state.weight)
            assertTrue(state.isEditing)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `save updates the edited dog instead of adding one`() = runTest {
        repository.addDogs(Dog(id = 7, name = "Sixten", breed = "Border collie", weightKg = 18.0))
        val editing = editViewModel(7)
        editing.uiState.test {
            awaitItem()
            editing.setName("Sixten II")
            editing.setWeight("19,5")
            editing.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        assertEquals(
            listOf(Dog(id = 7, name = "Sixten II", breed = "Border collie", weightKg = 19.5)),
            repository.observeDogs().first(),
        )
    }

    @Test
    fun `delete removes the edited dog and flags completion`() = runTest {
        repository.addDogs(Dog(id = 7, name = "Sixten"))
        val editing = editViewModel(7)
        editing.uiState.test {
            awaitItem()
            editing.delete()
            assertTrue(expectMostRecentItem().isDeleted)
        }
        assertEquals(emptyList<Dog>(), repository.observeDogs().first())
    }

    @Test
    fun `delete does nothing when registering a new dog`() = runTest {
        viewModel.delete()
        assertFalse(viewModel.uiState.value.isDeleted)
    }

    @Test
    fun `a failed save keeps the form open and says so`() = runTest {
        viewModel.uiState.test {
            viewModel.setName("Sixten")
            repository.failures.failWrites = true
            viewModel.save()
            val state = expectMostRecentItem()
            assertFalse(state.isSaving)
            assertFalse(state.isSaved)
            assertTrue(state.canSave)
            assertEquals(R.string.error_save_dog, state.userMessage)
        }
        assertEquals(listOf("Failed to save dog"), errorReporter.reported.map { it.second })
    }

    @Test
    fun `a shown message is cleared unless a newer one replaced it`() = runTest {
        viewModel.setName("Sixten")
        repository.failures.failWrites = true
        viewModel.save()
        viewModel.userMessageShown(R.string.error_delete_dog)
        assertEquals(R.string.error_save_dog, viewModel.uiState.value.userMessage)
        viewModel.userMessageShown(R.string.error_save_dog)
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun `a failed delete keeps the dog and says so`() = runTest {
        repository.addDogs(Dog(id = 7, name = "Sixten"))
        val editing = editViewModel(7)
        editing.uiState.test {
            repository.failures.failWrites = true
            editing.delete()
            val state = expectMostRecentItem()
            assertFalse(state.isDeleting)
            assertFalse(state.isDeleted)
            assertTrue(state.canDelete)
            assertEquals(R.string.error_delete_dog, state.userMessage)
        }
        assertEquals(listOf("Failed to delete dog"), errorReporter.reported.map { it.second })
    }

    @Test
    fun `editing a missing dog closes the screen`() = runTest {
        editViewModel(7).uiState.test {
            assertTrue(expectMostRecentItem().isNotFound)
        }
    }

    @Test
    fun `delete keeps the dog as deleted and hands over to the next dog`() = runTest {
        repository.addDogs(Dog(id = 1, name = "Sixten"), Dog(id = 2, name = "Mira"))
        repository.setActiveDog(1)
        val viewModel = editViewModel(dogId = 1)

        viewModel.delete()

        assertEquals(listOf(true, false), repository.observeAllDogs().first().map { it.isDeleted })
        assertEquals("Mira", repository.observeActiveDog().first()?.name)
    }

    @Test
    fun `a dog on an ongoing walk cannot be deleted`() = runTest {
        repository.addDogs(Dog(id = 1, name = "Sixten"))
        walkRepository.addWalks(ongoingWalk(dogId = 1))
        val viewModel = editViewModel(dogId = 1)

        assertFalse(viewModel.uiState.value.canDelete)
        viewModel.delete()
        assertEquals(false, repository.observeAllDogs().first().single().isDeleted)
    }

    @Test
    fun `another dog's ongoing walk does not block deleting`() = runTest {
        repository.addDogs(Dog(id = 1, name = "Sixten"))
        walkRepository.addWalks(ongoingWalk(dogId = 2))

        assertTrue(editViewModel(dogId = 1).uiState.value.canDelete)
    }
}
