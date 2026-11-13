package com.example.walklogger.feature.logwalk.newwalk

import androidx.annotation.StringRes
import com.example.walklogger.core.model.Equipment
import java.time.LocalDate

data class NewWalkUiState(
    /** The day chosen on the log screen. */
    val date: LocalDate,
    val dogName: String? = null,
    val handlers: List<String> = emptyList(),
    val selectedHandlers: Set<String> = emptySet(),
    val equipment: Equipment = Equipment.COLLAR,
    val isStarting: Boolean = false,
    /** The first start asks for location before the walk is stored. */
    val shouldRequestLocationPermission: Boolean = false,
    /** Set once the walk is stored; the timer screen opens for it. */
    val startedWalkId: Long? = null,
    @param:StringRes val userMessage: Int? = null,
) {
    /** A walk always has at least one handler. */
    val canStart: Boolean get() = selectedHandlers.isNotEmpty() && !isStarting &&
        startedWalkId == null
}
