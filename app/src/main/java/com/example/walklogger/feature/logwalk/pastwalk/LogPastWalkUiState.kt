package com.example.walklogger.feature.logwalk.pastwalk

import androidx.annotation.StringRes
import com.example.walklogger.feature.logwalk.SaveBlocker
import com.example.walklogger.feature.logwalk.WalkFormState
import com.example.walklogger.feature.logwalk.WalkTimesState

data class LogPastWalkUiState(
    /** The date starts as the day chosen on the log screen; the times are always entered by hand. */
    val times: WalkTimesState,
    /** The active dog, shown in the header; a new walk is logged for it. */
    val dogId: Long? = null,
    val dogName: String? = null,
    val handlers: List<String> = emptyList(),
    val selectedHandlers: Set<String> = emptySet(),
    val form: WalkFormState = WalkFormState(),
    /** True when the form changes an existing walk rather than logging a new one. */
    val isEditing: Boolean = false,
    /** True while the walk being edited is still being read; the form is not editable until it arrives. */
    val isLoading: Boolean = false,
    /** True when the walk no longer exists; the screen closes. */
    val isNotFound: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    @param:StringRes val userMessage: Int? = null,
) {
    /** The first thing still stopping the walk from being saved, in form order. */
    val saveBlocker: SaveBlocker? = when {
        selectedHandlers.isEmpty() -> SaveBlocker.HANDLER
        times.blocker != null -> times.blocker
        form.rating == null -> SaveBlocker.RATING
        else -> null
    }

    val canSave: Boolean = saveBlocker == null && (isEditing || dogId != null) &&
        !isLoading && !isSaving && !isSaved
}
