package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class LogPastWalkUiState(
    /** Prefilled with the day chosen on the log screen; the times are always entered by hand. */
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val dogName: String? = null,
    val handlers: List<String> = emptyList(),
    val selectedHandlers: Set<String> = emptySet(),
    val rating: Int? = null,
    val equipment: Equipment = Equipment.HARNESS,
    val rain: Rain? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
    val notes: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    /** Null until both times are set; zero or negative when the end is not after the start. */
    val durationMinutes: Int? =
        if (startTime != null && endTime != null) Duration.between(startTime, endTime).toMinutes().toInt() else null

    /** The first thing still stopping the walk from being saved, in form order. */
    val saveBlocker: SaveBlocker? = when {
        selectedHandlers.isEmpty() -> SaveBlocker.HANDLER
        durationMinutes == null -> SaveBlocker.TIMES
        durationMinutes <= 0 -> SaveBlocker.END_BEFORE_START
        rating == null -> SaveBlocker.RATING
        else -> null
    }

    val canSave: Boolean = saveBlocker == null && !isSaving && !isSaved
}

enum class SaveBlocker { HANDLER, TIMES, END_BEFORE_START, RATING }
