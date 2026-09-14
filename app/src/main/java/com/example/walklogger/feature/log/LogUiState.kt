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
) {
    val averageRating: Double? =
        walks.mapNotNull { it.rating }.takeIf { it.isNotEmpty() }?.average()
}

data class DayUiState(
    val date: LocalDate,
    val hasWalks: Boolean,
    val isSelected: Boolean,
)

data class WalkUiState(
    val id: Long,
    val startTime: LocalTime,
    val duration: Duration,
    val equipment: Equipment?,
    val rating: Int?,
    val note: String,
)
