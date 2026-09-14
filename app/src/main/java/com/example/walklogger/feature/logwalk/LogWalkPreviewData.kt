package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import java.time.LocalDate
import java.time.LocalTime

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

    val logPastWalk = LogPastWalkUiState(
        date = LocalDate.of(2026, 9, 12),
        startTime = LocalTime.of(17, 40),
        endTime = LocalTime.of(18, 5),
        dogName = "Sixten",
        handlers = handlers,
        selectedHandlers = setOf("Jonas"),
        equipment = Equipment.COLLAR,
        rain = Rain.LIGHT,
        temperature = Temperature.COLD,
        wind = Wind.WINDY,
    )
}
