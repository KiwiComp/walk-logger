package com.example.walklogger.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Walk
import com.example.walklogger.domain.ObserveWalkHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class CalendarViewModel @Inject constructor(
    observeWalkHistory: ObserveWalkHistoryUseCase,
    dogRepository: DogRepository,
    private val clock: Clock,
) : ViewModel() {

    /** The shown month is the one containing this date. */
    private val selectedDate = MutableStateFlow(LocalDate.now(clock))

    val uiState: StateFlow<CalendarUiState> =
        combine(selectedDate, observeWalkHistory(), dogRepository.observeActiveDog()) {
                date,
                walks,
                dog,
            ->
            buildState(date, walks, dog)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(selectedDate.value, emptyList(), activeDog = null),
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

    private fun buildState(date: LocalDate, walks: List<Walk>, activeDog: Dog?): CalendarUiState {
        val walksByDate = walks.groupBy { it.startDate(clock.zone) }
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
                    isEnabled = !day.isAfter(today),
                )
            },
            walks = walksByDate[date].orEmpty()
                .sortedBy { it.startTime }
                .map { it.toUiState(clock.zone) },
            isCurrentMonth = month == YearMonth.from(today),
            canStartLiveWalk = date == today,
            activeDogName = activeDog?.name,
            isActiveDogDeleted = activeDog?.isDeleted == true,
        )
    }
}

private fun Walk.startDate(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(startTime).atZone(zone).toLocalDate()

private fun Walk.toUiState(zone: ZoneId) = CalendarWalkUiState(
    id = id,
    startTime = Instant.ofEpochMilli(startTime).atZone(zone).toLocalTime(),
    equipment = equipment,
    rating = rating,
    note = notes,
)
