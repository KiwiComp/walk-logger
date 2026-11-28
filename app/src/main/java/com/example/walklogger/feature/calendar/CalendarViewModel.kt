package com.example.walklogger.feature.calendar

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
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt
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
class CalendarViewModel @Inject constructor(
    observeWalkHistory: ObserveWalkHistoryUseCase,
    private val observeWalkDistances: ObserveWalkDistancesUseCase,
    dogRepository: DogRepository,
    private val clock: Clock,
) : ViewModel() {

    /** The shown month is the one containing this date. */
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

    val uiState: StateFlow<CalendarUiState> =
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

    fun showPreviousMonth() = selectedDate.update { landingDay(YearMonth.from(it).minusMonths(1)) }

    fun showNextMonth() = selectedDate.update { date ->
        val next = YearMonth.from(date).plusMonths(1)
        if (next > YearMonth.now(clock)) date else landingDay(next)
    }

    fun showCurrentMonth() = selectedDate.update { landingDay(YearMonth.now(clock)) }

    /** Arriving in a month selects today if it is the current one, otherwise the 1st (never in the future). */
    private fun landingDay(month: YearMonth): LocalDate = if (month ==
        YearMonth.now(clock)
    ) {
        LocalDate.now(clock)
    } else {
        month.atDay(1)
    }

    private fun buildState(
        date: LocalDate,
        walks: List<Walk>,
        distances: Map<Long, Double>,
        activeDog: Dog?,
    ): CalendarUiState {
        val walksByDate = walks.groupByStartDate(clock.zone)
        val today = LocalDate.now(clock)
        val month = YearMonth.from(date)
        return CalendarUiState(
            month = month,
            selectedDate = date,
            days = (1..month.lengthOfMonth()).map(month::atDay).map { day ->
                val dayWalks = walksByDate[day].orEmpty()
                CalendarDayUiState(
                    date = day,
                    walkCount = dayWalks.size,
                    rating = dayWalks.mapNotNull {
                        it.rating
                    }.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
                    isSelected = day == date,
                    isToday = day == today,
                    isEnabled = !day.isAfter(today),
                )
            },
            walks = walksByDate[date].orEmpty().map { it.toUiState(clock.zone, distances[it.id]) },
            isCurrentMonth = month == YearMonth.from(today),
            canStartLiveWalk = date == today,
            activeDogName = activeDog?.name,
            isActiveDogDeleted = activeDog?.isDeleted == true,
        )
    }
}

private fun Walk.toUiState(zone: ZoneId, distanceMeters: Double?) = TimelineWalk(
    id = id,
    startTime = startLocalTime(zone),
    equipment = equipment,
    rating = rating,
    note = notes,
    duration = (checkNotNull(endTime) { "walk $id has not finished" } - startTime).milliseconds,
    distanceMeters = distanceMeters,
    hasPassedStool = stool?.passed,
)
