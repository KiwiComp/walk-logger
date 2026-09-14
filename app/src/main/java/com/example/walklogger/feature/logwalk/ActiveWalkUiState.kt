package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Equipment
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
    val isEnding: Boolean = false,
    val isEnded: Boolean = false,
) {
    val canEnd: Boolean get() = startTime != null && !isEnding && !isEnded
}
