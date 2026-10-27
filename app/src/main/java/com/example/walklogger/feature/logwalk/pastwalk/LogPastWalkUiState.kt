package com.example.walklogger.feature.logwalk.pastwalk

import com.example.walklogger.feature.logwalk.SaveBlocker
import com.example.walklogger.feature.logwalk.WalkFormState
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


data class LogPastWalkUiState(
    /** Prefilled with the day chosen on the log screen; the times are always entered by hand. */
    val date: LocalDate,
    /** The walk must have ended by now; refreshed by the ViewModel on every edit. */
    val now: LocalDateTime,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val dogName: String? = null,
    val handlers: List<String> = emptyList(),
    val selectedHandlers: Set<String> = emptySet(),
    val form: WalkFormState = WalkFormState(),
    /** True when the form changes an existing walk rather than logging a new one. */
    val isEditing: Boolean = false,
    /** True while the walk being edited is still being read; the form is not editable until it arrives. */
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    /** Null until both times are set; zero or negative when the end is not after the start. */
    val durationMinutes: Int? =
        if (startTime != null && endTime != null) Duration.between(startTime, endTime).toMinutes().toInt() else null

    /** Latest day the date picker offers. */
    val maxDate: LocalDate = now.toLocalDate()

    /** The first thing still stopping the walk from being saved, in form order. */
    val saveBlocker: SaveBlocker? = when {
        selectedHandlers.isEmpty() -> SaveBlocker.HANDLER
        durationMinutes == null -> SaveBlocker.TIMES
        durationMinutes <= 0 -> SaveBlocker.END_BEFORE_START
        date.atTime(endTime) > now -> SaveBlocker.IN_FUTURE
        form.rating == null -> SaveBlocker.RATING
        else -> null
    }

    val canSave: Boolean = saveBlocker == null && !isLoading && !isSaving && !isSaved
}
