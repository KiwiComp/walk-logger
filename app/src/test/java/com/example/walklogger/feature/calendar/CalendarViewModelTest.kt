package com.example.walklogger.feature.calendar

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Walk
import com.example.walklogger.domain.ObserveWalkHistoryUseCase
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 12)
    private val clock = Clock.fixed(today.atTime(15, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val repository = FakeWalkRepository()
    private val dogRepository = FakeDogRepository()

    /** Lazy so viewModelScope picks up the rule's Main dispatcher. */
    private val viewModel by lazy { CalendarViewModel(ObserveWalkHistoryUseCase(repository, dogRepository), dogRepository, clock) }

    private fun walk(id: Long, start: LocalDateTime, rating: Int? = null, finished: Boolean = true, dogId: Long = 1) =
        Walk(
            id = id,
            dogId = dogId,
            handlers = listOf("Kim"),
            startTime = start.toEpochSecond(ZoneOffset.UTC) * 1000,
            endTime = if (finished) start.plusMinutes(30).toEpochSecond(ZoneOffset.UTC) * 1000 else null,
            equipment = Equipment.HARNESS,
            rating = rating,
        )

    @Test
    fun `today is selected in the current month and future days are disabled`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(YearMonth.of(2026, 9), state.month)
            assertEquals(today, state.selectedDate)
            assertEquals((1..30).map { LocalDate.of(2026, 9, it) }, state.days.map { it.date })
            assertEquals(today, state.days.single { it.isSelected }.date)
            assertEquals((1..12).map { LocalDate.of(2026, 9, it) }, state.days.filter { it.isEnabled }.map { it.date })
            assertEquals(0, state.monthWalkCount)
            assertEquals(emptyList<CalendarWalkUiState>(), state.walks)
            assertTrue(state.isCurrentMonth)
            assertFalse(state.canShowNextMonth)
            assertTrue(state.canStartLiveWalk)
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
    fun `days carry their walk count and rounded average rating`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Mira"))
        repository.addWalks(
            walk(id = 1, start = today.atTime(7, 5), rating = 6),
            walk(id = 2, start = today.atTime(11, 30), rating = 3),
            walk(id = 3, start = today.minusDays(2).atTime(9, 0), rating = 2),
            walk(id = 4, start = today.minusDays(3).atTime(9, 0), finished = false),
            walk(id = 5, start = today.minusMonths(1).atTime(9, 0), rating = 7),
        )
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            val byDay = state.days.associateBy { it.date.dayOfMonth }
            assertEquals(2, byDay.getValue(12).walkCount)
            assertEquals(5, byDay.getValue(12).rating)
            assertEquals(1, byDay.getValue(10).walkCount)
            assertEquals(2, byDay.getValue(10).rating)
            assertEquals(0, byDay.getValue(9).walkCount)
            assertNull(byDay.getValue(9).rating)
            assertEquals(3, state.monthWalkCount)
        }
    }

    @Test
    fun `walks on the selected day are listed earliest first`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Mira"))
        repository.addWalks(
            walk(id = 1, start = today.atTime(11, 30), rating = 4),
            walk(id = 2, start = today.atTime(7, 5), rating = 6),
            walk(id = 3, start = today.minusDays(1).atTime(9, 0)),
        )
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf(2L, 1L), state.walks.map { it.id })
            assertEquals(LocalTime.of(7, 5), state.walks[0].startTime)
            assertEquals(5.0, state.averageRating!!, 0.0)
        }
    }

    @Test
    fun `selecting a day shows its walks`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Mira"))
        val friday = today.minusDays(1)
        repository.addWalks(walk(id = 1, start = friday.atTime(9, 0)))
        viewModel.uiState.test {
            assertEquals(emptyList<CalendarWalkUiState>(), awaitItem().walks)
            viewModel.selectDay(friday)
            val state = expectMostRecentItem()
            assertEquals(friday, state.selectedDate)
            assertEquals(listOf(1L), state.walks.map { it.id })
            assertEquals(friday, state.days.single { it.isSelected }.date)
            assertFalse(state.canStartLiveWalk)
        }
    }

    @Test
    fun `previous month selects its first day and moving forward to the current month selects today`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.showPreviousMonth()
            val august = expectMostRecentItem()
            assertEquals(YearMonth.of(2026, 8), august.month)
            assertEquals(LocalDate.of(2026, 8, 1), august.selectedDate)
            assertEquals(31, august.days.size)
            assertTrue(august.days.all { it.isEnabled })
            assertFalse(august.isCurrentMonth)
            assertTrue(august.canShowNextMonth)
            assertFalse(august.canStartLiveWalk)
            viewModel.showNextMonth()
            val september = expectMostRecentItem()
            assertEquals(YearMonth.of(2026, 9), september.month)
            assertEquals(today, september.selectedDate)
            assertTrue(september.canStartLiveWalk)
        }
    }

    @Test
    fun `today jumps back to the current month with today selected`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            repeat(3) { viewModel.showPreviousMonth() }
            assertEquals(YearMonth.of(2026, 6), expectMostRecentItem().month)
            viewModel.showCurrentMonth()
            val state = expectMostRecentItem()
            assertEquals(YearMonth.of(2026, 9), state.month)
            assertEquals(today, state.selectedDate)
            assertTrue(state.isCurrentMonth)
        }
    }

    @Test
    fun `next month never goes past the current month`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.showNextMonth()
            expectNoEvents()
            assertEquals(today, viewModel.uiState.value.selectedDate)
        }
    }

    @Test
    fun `only the active dog's walks are shown and switching dog switches them`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Mira"), Dog(id = 2, name = "Sixten"))
        dogRepository.setActiveDog(1)
        repository.addWalks(
            walk(id = 1, start = today.atTime(8, 0), rating = 6, dogId = 1),
            walk(id = 2, start = today.atTime(9, 0), rating = 2, dogId = 2),
            walk(id = 3, start = today.minusDays(1).atTime(8, 0), dogId = 2),
        )
        viewModel.uiState.test {
            val mira = expectMostRecentItem()
            assertEquals(listOf(1L), mira.walks.map { it.id })
            assertEquals(1, mira.monthWalkCount)
            assertEquals(6, mira.days.single { it.date == today }.rating)
            dogRepository.setActiveDog(2)
            val sixten = expectMostRecentItem()
            assertEquals(listOf(2L), sixten.walks.map { it.id })
            assertEquals(2, sixten.monthWalkCount)
            assertEquals(2, sixten.days.single { it.date == today }.rating)
        }
    }

    @Test
    fun `no walks are shown without an active dog`() = runTest {
        repository.addWalks(walk(id = 1, start = today.atTime(8, 0)))
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(emptyList<CalendarWalkUiState>(), state.walks)
            assertEquals(0, state.monthWalkCount)
        }
    }
}
