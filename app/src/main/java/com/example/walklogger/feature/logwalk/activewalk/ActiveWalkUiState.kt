package com.example.walklogger.feature.logwalk.activewalk

import androidx.annotation.StringRes
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Stool
import java.time.Duration
import java.time.LocalTime

data class ActiveWalkUiState(
    val dogName: String? = null,
    val handlers: List<String> = emptyList(),
    val equipment: Equipment? = null,
    /** Null until the walk has been read from the repository. */
    val startTime: LocalTime? = null,
    /** Time since the walk started; advances once a second. */
    val elapsed: Duration = Duration.ZERO,
    val note: String = "",
    /** Null until the stool switch is turned on; No is only answered in the after-walk form. */
    val stool: Stool? = null,
    /** True while the description rows are shown; false folds them into a summary line. */
    val isStoolExpanded: Boolean = false,
    val isEnding: Boolean = false,
    val isEnded: Boolean = false,
    val isDiscarding: Boolean = false,
    /** True once the walk has been deleted; nothing of it remains to log. */
    val isDiscarded: Boolean = false,
    /** True when the walk no longer exists; the screen closes. */
    val isNotFound: Boolean = false,
    @param:StringRes val userMessage: Int? = null,
) {
    private val isBusy: Boolean get() = isEnding || isEnded || isDiscarding || isDiscarded

    val canEnd: Boolean get() = startTime != null && !isBusy

    val canDiscard: Boolean get() = startTime != null && !isBusy
}
