package com.example.walklogger.feature.log

import com.example.walklogger.core.ui.component.TimelineWalk
import java.time.LocalDate
import kotlin.time.Duration

data class LogUiState(
    val selectedDate: LocalDate,
    /** ISO week number of [selectedDate], matching the Monday-to-Sunday [week]. */
    val weekNumber: Int,
    /** Monday to Sunday of the week containing [selectedDate]. */
    val week: List<DayUiState>,
    /** Completed walks on [selectedDate], earliest first. */
    val walks: List<TimelineWalk>,
    /** A live walk can only be started today; past days go straight to the past-walk form. */
    val canStartLiveWalk: Boolean,
    /** Null until a dog has been registered. */
    val activeDogName: String? = null,
    val isActiveDogDeleted: Boolean = false,
) {
    val averageRating: Double? =
        walks.mapNotNull { it.rating }.takeIf { it.isNotEmpty() }?.average()

    /** Walks can only be logged for a registered dog that is not deleted. */
    val canAddWalk: Boolean get() = activeDogName != null && !isActiveDogDeleted

    val totalDuration: Duration = walks.fold(Duration.ZERO) { total, walk ->
        total + walk.duration
    }

    /** Null when none of the day's walks has a recorded route. */
    val totalDistanceMeters: Double? =
        walks.mapNotNull { it.distanceMeters }.takeIf { it.isNotEmpty() }?.sum()
}

data class DayUiState(
    val date: LocalDate,
    val hasWalks: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean,
    /** False for days after today: nothing can be logged there yet. */
    val isEnabled: Boolean,
)
