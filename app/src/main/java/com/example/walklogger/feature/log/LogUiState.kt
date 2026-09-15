package com.example.walklogger.feature.log

import com.example.walklogger.core.model.Equipment
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class LogUiState(
    val selectedDate: LocalDate,
    /** Monday to Sunday of the week containing [selectedDate]. */
    val week: List<DayUiState>,
    /** Completed walks on [selectedDate], earliest first. */
    val walks: List<WalkUiState>,
    /** A live walk can only be started today; past days go straight to the past-walk form. */
    val canStartLiveWalk: Boolean,
    /** Null until a dog has been registered. */
    val activeDogName: String? = null,
) {
    val averageRating: Double? =
        walks.mapNotNull { it.rating }.takeIf { it.isNotEmpty() }?.average()

    /** Walks can only be logged for a registered dog. */
    val canAddWalk: Boolean get() = activeDogName != null
}

data class DayUiState(
    val date: LocalDate,
    val hasWalks: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean,
    /** False for days after today: nothing can be logged there yet. */
    val isEnabled: Boolean,
)

data class WalkUiState(
    val id: Long,
    val startTime: LocalTime,
    val duration: Duration,
    val equipment: Equipment?,
    val rating: Int?,
    val note: String,
)
