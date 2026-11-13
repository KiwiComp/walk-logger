package com.example.walklogger.feature.logwalk.finishwalk

import com.example.walklogger.feature.logwalk.SaveBlocker
import com.example.walklogger.feature.logwalk.WalkFormState
import com.example.walklogger.feature.logwalk.WalkTimesState

/** The form shown right after a timed walk; the times were recorded but can still be corrected. */
data class FinishWalkUiState(
    val times: WalkTimesState,
    val dogName: String? = null,
    /** Chosen before the walk; not editable here. */
    val handlers: List<String> = emptyList(),
    val form: WalkFormState = WalkFormState(),
    /** True while the "Edit times" pickers are shown. */
    val isEditingTimes: Boolean = false,
    /** True until the finished walk has been read; the form is not editable before that. */
    val isLoading: Boolean = true,
    /** True when the walk no longer exists; the screen closes. */
    val isNotFound: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    /** The first thing still stopping the walk from being saved, in form order. */
    val saveBlocker: SaveBlocker? =
        times.blocker ?: if (form.rating == null) SaveBlocker.RATING else null

    val canSave: Boolean = saveBlocker == null && !isLoading && !isSaving && !isSaved
}
