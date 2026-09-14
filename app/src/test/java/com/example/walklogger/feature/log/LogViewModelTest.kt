package com.example.walklogger.feature.log

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Walk
import com.example.walklogger.domain.ObserveWalkHistoryUseCase
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 12)
    private val clock = Clock.fixed(today.atTime(15, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val repository = FakeWalkRepository()
    private val dogRepository = FakeDogRepository()

    /** Lazy so viewModelScope picks up the rule's Main dispatcher. */
    private val viewModel by lazy { LogViewModel(ObserveWalkHistoryUseCase(repository), dogRepository, clock) }

    private fun walk(id: Long, start: LocalDateTime, minutes: Long = 30, rating: Int? = null, finished: Boolean = true) =
        Walk(
            id = id,
            handler = "Kim",
            startTime = start.toEpochSecond(ZoneOffset.UTC) * 1000,
            endTime = if (finished) start.plusMinutes(minutes).toEpochSecond(ZoneOffset.UTC) * 1000 else null,
            equipment = Equipment.HARNESS,
            rating = rating,
        )

    @Test
    fun `today is selected and the week runs Monday to Sunday`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(today, state.selectedDate)
            assertEquals((7..13).map { LocalDate.of(2026, 9, it) }, state.week.map { it.date })
            assertEquals(listOf(false, false, false, false, false, true, false), state.week.map { it.isSelected })
            assertEquals(List(7) { false }, state.week.map { it.hasWalks })
            assertEquals(listOf(true, true, true, true, true, true, false), state.week.map { it.isEnabled })
            assertEquals(today, state.week.single { it.isToday }.date)
            assertEquals(emptyList<WalkUiState>(), state.walks)
            assertNull(state.averageRating)
            assertNull(state.activeDogName)
        }
    }

    @Test
    fun `active dog name is shown once a dog is registered`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Mira"))
        viewModel.uiState.test {
            assertEquals("Mira", expectMostRecentItem().activeDogName)
        }
    }

    @Test
    fun `walks on the selected day are listed earliest first`() = runTest {
        repository.addWalks(
            walk(id = 1, start = today.atTime(11, 30), minutes = 17, rating = 4),
            walk(id = 2, start = today.atTime(7, 5), minutes = 38, rating = 6),
            walk(id = 3, start = today.minusDays(1).atTime(9, 0)),
        )
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf(2L, 1L), state.walks.map { it.id })
            assertEquals(LocalTime.of(7, 5), state.walks[0].startTime)
            assertEquals(Duration.ofMinutes(38), state.walks[0].duration)
            assertEquals(5.0, state.averageRating!!, 0.0)
            assertEquals(listOf(false, false, false, false, true, true, false), state.week.map { it.hasWalks })
        }
    }

    @Test
    fun `active walks are excluded`() = runTest {
        repository.addWalks(walk(id = 1, start = today.atTime(8, 0), finished = false))
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(emptyList<WalkUiState>(), state.walks)
            assertEquals(false, state.week.single { it.date == today }.hasWalks)
        }
    }

    @Test
    fun `selecting a day shows its walks`() = runTest {
        val friday = today.minusDays(1)
        repository.addWalks(walk(id = 1, start = friday.atTime(9, 0)))
        viewModel.uiState.test {
            assertEquals(emptyList<WalkUiState>(), awaitItem().walks)
            viewModel.selectDay(friday)
            val state = expectMostRecentItem()
            assertEquals(friday, state.selectedDate)
            assertEquals(listOf(1L), state.walks.map { it.id })
            assertEquals(friday, state.week.single { it.isSelected }.date)
            assertEquals(today, state.week.single { it.isToday }.date)
        }
    }
}
