package com.example.walklogger.feature.dog

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.MainDispatcherRule
import com.example.walklogger.testing.shownMessages
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
    private val userMessages = UserMessages()

    private val viewModel by lazy { DogViewModel(repository, userMessages, clock) }

    private val sixten =
        Dog(
            id = 1,
            name = "Sixten",
            breed = "Border collie",
            birthDate = today.minusYears(3).minusDays(10),
            weightKg = 18.0,
        )
    private val mira = Dog(id = 2, name = "Mira")

    @Test
    fun `no dog registered shows the empty state`() = runTest {
        viewModel.uiState.test {
            assertEquals(DogUiState.NoDog(), expectMostRecentItem())
        }
    }

    @Test
    fun `first dog is active and its profile is shown`() = runTest {
        repository.addDogs(sixten, mira)
        viewModel.uiState.test {
            val state = expectMostRecentItem() as DogUiState.Profile
            assertEquals(
                DogProfileUiState(
                    id = 1,
                    name = "Sixten",
                    breed = "Border collie",
                    ageYears = 3,
                    weightKg = 18.0,
                ),
                state.active,
            )
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
            assertEquals(
                DogProfileUiState(
                    id = 2,
                    name = "Mira",
                    breed = "",
                    ageYears = null,
                    weightKg = null,
                ),
                state.active,
            )
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

    @Test
    fun `a failed switch keeps the profile and says so`() = runTest {
        repository.addDogs(sixten, mira)
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            repository.failures.failWrites = true
            viewModel.switchDog(2)
            assertEquals("Sixten", (expectMostRecentItem() as DogUiState.Profile).active.name)
        }
        assertEquals(listOf(R.string.error_select_dog), shown)
    }

    @Test
    fun `deleted dogs are listed apart and can be selected as active`() = runTest {
        repository.addDogs(sixten, mira.copy(isDeleted = true))
        viewModel.uiState.test {
            val before = expectMostRecentItem() as DogUiState.Profile
            assertEquals(
                listOf(DogListItemUiState(id = 1, name = "Sixten", isActive = true)),
                before.dogs,
            )
            assertEquals(
                listOf(DogListItemUiState(id = 2, name = "Mira", isActive = false)),
                before.deletedDogs,
            )
            viewModel.switchDog(2)
            val after = expectMostRecentItem() as DogUiState.Profile
            assertEquals("Mira", after.active.name)
            assertEquals(true, after.active.isDeleted)
            assertEquals(
                listOf(DogListItemUiState(id = 2, name = "Mira", isActive = true)),
                after.deletedDogs,
            )
        }
    }

    @Test
    fun `only deleted dogs shows the empty state with them listed`() = runTest {
        repository.addDogs(mira.copy(isDeleted = true))
        viewModel.uiState.test {
            assertEquals(
                DogUiState.NoDog(
                    deletedDogs = listOf(
                        DogListItemUiState(id = 2, name = "Mira", isActive = false),
                    ),
                ),
                expectMostRecentItem(),
            )
        }
    }
}
