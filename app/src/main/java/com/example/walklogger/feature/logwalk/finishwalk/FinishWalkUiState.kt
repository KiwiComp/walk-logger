package com.example.walklogger.feature.logwalk.finishwalk

import androidx.annotation.StringRes
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.feature.logwalk.SaveBlocker
import com.example.walklogger.feature.logwalk.WalkFormState
import com.example.walklogger.feature.logwalk.WalkTimesState

/** The form shown right after a timed walk; the times were recorded but can still be corrected. */
data class FinishWalkUiState(
    val times: WalkTimesState,
    val dogName: String? = null,
    /** Every stored handler, for the Change chips. */
    val handlers: List<String> = emptyList(),
    /** Chosen before the walk; Change lets the handler correct it. */
    val selectedHandlers: Set<String> = emptySet(),
    /** True while the handler chips are shown. */
    val isEditingHandlers: Boolean = false,
    val form: WalkFormState = WalkFormState(),
    /** The recorded route within the times as currently edited; empty for a walk without one. */
    val route: List<Coordinates> = emptyList(),
    /** True while the "Edit times" pickers are shown. */
    val isEditingTimes: Boolean = false,
    /** True until the finished walk has been read; the form is not editable before that. */
    val isLoading: Boolean = true,
    /** True when the walk no longer exists; the screen closes. */
    val isNotFound: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDiscarding: Boolean = false,
    /** True once the walk has been deleted; the screen closes. */
    val isDiscarded: Boolean = false,
    @param:StringRes val userMessage: Int? = null,
) {
    /** The first thing still stopping the walk from being saved, in form order. */
    val saveBlocker: SaveBlocker? = when {
        !isLoading && selectedHandlers.isEmpty() -> SaveBlocker.HANDLER
        times.blocker != null -> times.blocker
        form.rating == null -> SaveBlocker.RATING
        else -> null
    }

    val canSave: Boolean =
        saveBlocker == null && !isLoading && !isSaving && !isSaved && !isDiscarding

    val canDiscard: Boolean = !isLoading && !isSaving && !isDiscarding && !isDiscarded
}
