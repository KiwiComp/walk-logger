package com.example.walklogger.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Walk
import com.example.walklogger.domain.ObserveWalkHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class LogViewModel @Inject constructor(
    observeWalkHistory: ObserveWalkHistoryUseCase,
    dogRepository: DogRepository,
    private val clock: Clock,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now(clock))

    val uiState: StateFlow<LogUiState> =
        combine(selectedDate, observeWalkHistory(), dogRepository.observeActiveDog()) { date, walks, dog ->
            buildState(date, walks, dog)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(selectedDate.value, emptyList(), activeDog = null),
        )

    fun selectDay(date: LocalDate) = selectedDate.update { date }

    fun addWalk() {
        println("Add walk pressed for ${selectedDate.value}")
    }

    private fun buildState(date: LocalDate, walks: List<Walk>, activeDog: Dog?): LogUiState {
        val walksByDate = walks.groupBy { it.startDate(clock.zone) }
        val today = LocalDate.now(clock)
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return LogUiState(
            selectedDate = date,
            week = (0L until 7L).map { monday.plusDays(it) }.map { day ->
                DayUiState(
                    date = day,
                    hasWalks = day in walksByDate,
                    isSelected = day == date,
                    isToday = day == today,
                    isEnabled = !day.isAfter(today),
                )
            },
            walks = walksByDate[date].orEmpty()
                .sortedBy { it.startTime }
                .map { it.toUiState(clock.zone) },
            activeDogName = activeDog?.name,
        )
    }
}

private fun Walk.startDate(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(startTime).atZone(zone).toLocalDate()

private fun Walk.toUiState(zone: ZoneId) = WalkUiState(
    id = id,
    startTime = Instant.ofEpochMilli(startTime).atZone(zone).toLocalTime(),
    duration = Duration.ofMillis(checkNotNull(endTime) { "walk $id has not finished" } - startTime),
    equipment = equipment,
    rating = rating,
    note = notes,
)
