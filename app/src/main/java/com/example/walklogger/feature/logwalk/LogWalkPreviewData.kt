package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Equipment
import java.time.LocalDate

/** Fixed content for `@Preview`s only; never reaches the running app. */
internal object LogWalkPreviewData {
    val handlers = listOf("Anna", "Jonas", "Elin")

    val newWalk = NewWalkUiState(
        date = LocalDate.of(2026, 9, 12),
        isToday = true,
        dogName = "Sixten",
        handlers = handlers,
        selectedHandlers = setOf("Anna", "Jonas"),
        equipment = Equipment.HARNESS,
    )
}
