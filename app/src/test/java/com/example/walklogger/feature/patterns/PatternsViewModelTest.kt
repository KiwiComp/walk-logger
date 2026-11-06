package com.example.walklogger.feature.patterns

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PatternsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeDogRepository()
    private val viewModel by lazy { PatternsViewModel(repository) }

    @Test
    fun `badge is hidden until a dog is registered`() = runTest {
        viewModel.uiState.test {
            assertNull(expectMostRecentItem().activeDogName)
            repository.addDogs(Dog(id = 1, name = "Mira"))
            assertEquals("Mira", expectMostRecentItem().activeDogName)
        }
    }

    @Test
    fun `a deleted active dog is flagged`() = runTest {
        repository.addDogs(Dog(id = 1, name = "Mira", isDeleted = true))
        repository.setActiveDog(1)
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Mira", state.activeDogName)
            assertEquals(true, state.isActiveDogDeleted)
        }
    }
}
