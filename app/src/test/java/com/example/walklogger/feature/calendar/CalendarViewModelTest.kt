package com.example.walklogger.feature.calendar

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeDogRepository()
    private val viewModel by lazy { CalendarViewModel(repository) }

    @Test
    fun `badge is hidden until a dog is registered`() = runTest {
        viewModel.uiState.test {
            assertNull(expectMostRecentItem().activeDogName)
            repository.addDogs(Dog(id = 1, name = "Mira"))
            assertEquals("Mira", expectMostRecentItem().activeDogName)
        }
    }
}
