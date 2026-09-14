package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Equipment
import java.time.LocalDate

data class NewWalkUiState(
    /** The day chosen on the log screen. */
    val date: LocalDate,
    /** Only today's walk can be timed; a past day can only be logged afterwards. */
    val isToday: Boolean,
    val dogName: String? = null,
    val handlers: List<String> = emptyList(),
    val selectedHandlers: Set<String> = emptySet(),
    val equipment: Equipment = Equipment.HARNESS,
) {
    /** A walk always has at least one handler. */
    val canStart: Boolean get() = selectedHandlers.isNotEmpty()
}
