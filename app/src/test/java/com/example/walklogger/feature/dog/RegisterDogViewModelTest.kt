package com.example.walklogger.feature.dog

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.testing.FakeDogRepository
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
    private val viewModel by lazy { RegisterDogViewModel(repository) }

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
            listOf(Dog(id = 1, name = "Sixten", breed = "Border collie", birthDate = birthDate, weightKg = 18.0)),
            repository.observeDogs().first(),
        )
    }

    @Test
    fun `save without a name does nothing`() = runTest {
        viewModel.save()
        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals(emptyList<Dog>(), repository.observeDogs().first())
    }
}
