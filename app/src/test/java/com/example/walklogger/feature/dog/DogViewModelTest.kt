package com.example.walklogger.feature.dog

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.MainDispatcherRule
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 12)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val repository = FakeDogRepository()

    private val viewModel by lazy { DogViewModel(repository, clock) }

    private val sixten = Dog(id = 1, name = "Sixten", breed = "Border collie", birthDate = today.minusYears(3).minusDays(10), weightKg = 18.0)
    private val mira = Dog(id = 2, name = "Mira")

    @Test
    fun `no dog registered shows the empty state`() = runTest {
        viewModel.uiState.test {
            assertEquals(DogUiState.NoDog, expectMostRecentItem())
        }
    }

    @Test
    fun `first dog is active and its profile is shown`() = runTest {
        repository.addDogs(sixten, mira)
        viewModel.uiState.test {
            val state = expectMostRecentItem() as DogUiState.Profile
            assertEquals(DogProfileUiState(name = "Sixten", breed = "Border collie", ageYears = 3, weightKg = 18.0), state.active)
            assertEquals(
                listOf(
                    DogListItemUiState(id = 1, name = "Sixten", isActive = true),
                    DogListItemUiState(id = 2, name = "Mira", isActive = false),
                ),
                state.dogs,
            )
        }
    }

    @Test
    fun `age is null without a birth date`() = runTest {
        repository.addDogs(mira)
        viewModel.uiState.test {
            val state = expectMostRecentItem() as DogUiState.Profile
            assertEquals(DogProfileUiState(name = "Mira", breed = "", ageYears = null, weightKg = null), state.active)
        }
    }

    @Test
    fun `switching dog changes the profile`() = runTest {
        repository.addDogs(sixten, mira)
        viewModel.uiState.test {
            assertEquals("Sixten", (expectMostRecentItem() as DogUiState.Profile).active.name)
            viewModel.switchDog(2)
            val state = expectMostRecentItem() as DogUiState.Profile
            assertEquals("Mira", state.active.name)
            assertEquals(listOf(false, true), state.dogs.map { it.isActive })
        }
    }
}
