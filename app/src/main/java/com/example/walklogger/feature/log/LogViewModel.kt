package com.example.walklogger.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.groupByStartDate
import com.example.walklogger.core.model.startDate
import com.example.walklogger.core.model.startLocalTime
import com.example.walklogger.core.ui.component.TimelineWalk
import com.example.walklogger.domain.ObserveWalkDistancesUseCase
import com.example.walklogger.domain.ObserveWalkHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class LogViewModel @Inject constructor(
    observeWalkHistory: ObserveWalkHistoryUseCase,
    private val observeWalkDistances: ObserveWalkDistancesUseCase,
    dogRepository: DogRepository,
    private val clock: Clock,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now(clock))

    private val walkHistory =
        observeWalkHistory().shareIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            replay = 1,
        )

    /** Only the selected day's routes are read; a new day cancels the previous day's query. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val distances = combine(selectedDate, walkHistory) { date, walks ->
        walks.filter { it.startDate(clock.zone) == date }
    }.distinctUntilChanged().flatMapLatest { observeWalkDistances(it) }

    val uiState: StateFlow<LogUiState> =
        combine(
            selectedDate,
            walkHistory,
            distances,
            dogRepository.observeActiveDog(),
        ) { date, walks, distances, dog ->
            buildState(date, walks, distances, dog)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(
                selectedDate.value,
                emptyList(),
                emptyMap(),
                activeDog = null,
            ),
        )

    fun selectDay(date: LocalDate) = selectedDate.update { date }

    private fun buildState(
        date: LocalDate,
        walks: List<Walk>,
        distances: Map<Long, Double>,
        activeDog: Dog?,
    ): LogUiState {
        val walksByDate = walks.groupByStartDate(clock.zone)
        val today = LocalDate.now(clock)
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return LogUiState(
            selectedDate = date,
            weekNumber = date.get(WeekFields.ISO.weekOfWeekBasedYear()),
            week = (0L until 7L).map { monday.plusDays(it) }.map { day ->
                DayUiState(
                    date = day,
                    hasWalks = day in walksByDate,
                    isSelected = day == date,
                    isToday = day == today,
                    isEnabled = !day.isAfter(today),
                )
            },
            walks = walksByDate[date].orEmpty().map { it.toUiState(clock.zone, distances[it.id]) },
            canStartLiveWalk = date == today,
            activeDogName = activeDog?.name,
            isActiveDogDeleted = activeDog?.isDeleted == true,
        )
    }
}

private fun Walk.toUiState(zone: ZoneId, distanceMeters: Double?) = TimelineWalk(
    id = id,
    startTime = startLocalTime(zone),
    duration = (checkNotNull(endTime) { "walk $id has not finished" } - startTime).milliseconds,
    equipment = equipment,
    rating = rating,
    note = notes,
    distanceMeters = distanceMeters,
    hasPassedStool = stool?.passed,
)
