package com.example.walklogger.feature.logwalk.finishwalk

import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import com.example.walklogger.feature.logwalk.SaveBlocker
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


/** The form shown right after a timed walk; the times were recorded but can still be corrected. */
data class FinishWalkUiState(
    /** The walk must have ended by now; refreshed by the ViewModel on every time edit. */
    val now: LocalDateTime,
    val date: LocalDate = now.toLocalDate(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val dogName: String? = null,
    /** Chosen before the walk; not editable here. */
    val handlers: List<String> = emptyList(),
    val rating: Int? = null,
    val optionalRatings: Map<OptionalRating, Int> = emptyMap(),
    /** True while the optional rating scales are shown; opens by itself when a stored walk has any. */
    val isOptionalRatingsExpanded: Boolean = false,
    /** Null until the handler answers Yes or No. */
    val stool: Stool? = null,
    val equipment: Equipment = Equipment.COLLAR,
    val rain: Rain? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
    val snow: Snow? = null,
    /** True while the optional weather rows are shown; opens by itself when a stored walk has any. */
    val isWeatherExpanded: Boolean = false,
    val issues: List<BehaviourIssue> = emptyList(),
    val selectedIssueIds: Set<Long> = emptySet(),
    /** True while the optional issue chips are shown; opens by itself when a stored walk has any. */
    val isBehaviourExpanded: Boolean = false,
    val concerns: List<MedicalConcern> = emptyList(),
    val selectedConcernIds: Set<Long> = emptySet(),
    /** True while the optional concern chips are shown; opens by itself when a stored walk has any. */
    val isMedicalExpanded: Boolean = false,
    val notes: String = "",
    /** True while the "Edit times" pickers are shown. */
    val isEditingTimes: Boolean = false,
    /** True until the finished walk has been read; the form is not editable before that. */
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    /** Null until loaded; zero or negative when the end is not after the start. */
    val durationMinutes: Int? =
        if (startTime != null && endTime != null) Duration.between(startTime, endTime).toMinutes().toInt() else null

    /** Latest day the date picker offers. */
    val maxDate: LocalDate = now.toLocalDate()

    /** The first thing still stopping the walk from being saved, in form order. */
    val saveBlocker: SaveBlocker? = when {
        durationMinutes == null -> SaveBlocker.TIMES
        durationMinutes <= 0 -> SaveBlocker.END_BEFORE_START
        date.atTime(endTime) > now -> SaveBlocker.IN_FUTURE
        rating == null -> SaveBlocker.RATING
        else -> null
    }

    val canSave: Boolean = saveBlocker == null && !isLoading && !isSaving && !isSaved
}
