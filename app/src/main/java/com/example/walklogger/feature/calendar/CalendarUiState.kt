package com.example.walklogger.feature.calendar

import com.example.walklogger.core.ui.component.TimelineWalk
import java.time.LocalDate
import java.time.YearMonth
import kotlin.time.Duration

data class CalendarUiState(
    val month: YearMonth,
    /** Always inside [month]. */
    val selectedDate: LocalDate,
    /** One entry per day of [month], in order. */
    val days: List<CalendarDayUiState>,
    /** Completed walks on [selectedDate], earliest first. */
    val walks: List<TimelineWalk>,
    val isCurrentMonth: Boolean,
    /** A live walk can only be started today; past days go straight to the past-walk form. */
    val canStartLiveWalk: Boolean,
    /** Null until a dog has been registered. */
    val activeDogName: String? = null,
    val isActiveDogDeleted: Boolean = false,
) {
    val monthWalkCount: Int = days.sumOf { it.walkCount }

    val isTodaySelected: Boolean = days.any { it.isSelected && it.isToday }

    /** There is nothing to show beyond today's month. */
    val canShowNextMonth: Boolean get() = !isCurrentMonth

    val totalDuration: Duration = walks.fold(Duration.ZERO) { total, walk ->
        total + walk.duration
    }

    /** Walks can only be logged for a registered dog that is not deleted. */
    val canAddWalk: Boolean get() = activeDogName != null && !isActiveDogDeleted
}

data class CalendarDayUiState(
    val date: LocalDate,
    val walkCount: Int,
    /** The day's average rating rounded to the nearest step; null when it has no rated walks. */
    val rating: Int?,
    val isSelected: Boolean,
    val isToday: Boolean,
    /** False for days after today: nothing can be logged there yet. */
    val isEnabled: Boolean,
)
