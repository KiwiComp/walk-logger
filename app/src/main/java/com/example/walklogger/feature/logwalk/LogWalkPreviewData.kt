package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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
        now = LocalDateTime.of(2026, 9, 14, 20, 0),
        startTime = LocalTime.of(17, 40),
        endTime = LocalTime.of(18, 5),
        dogName = "Sixten",
        handlers = handlers,
        selectedHandlers = setOf("Jonas"),
        equipment = Equipment.COLLAR,
        rain = Rain.LIGHT,
        temperature = Temperature.COLD,
        wind = Wind.WINDY,
        snow = Snow.LIGHT,
    )

    val activeWalk = ActiveWalkUiState(
        dogName = "Sixten",
        handlers = listOf("Anna"),
        equipment = Equipment.HARNESS,
        startTime = LocalTime.of(7, 5),
        elapsed = Duration.ofMinutes(34).plusSeconds(12),
        note = "Barked at the bin lorry",
    )

    val finishWalk = FinishWalkUiState(
        now = LocalDateTime.of(2026, 9, 12, 7, 39),
        date = LocalDate.of(2026, 9, 12),
        startTime = LocalTime.of(7, 5),
        endTime = LocalTime.of(7, 39),
        dogName = "Sixten",
        handlers = listOf("Anna"),
        rating = 6,
        equipment = Equipment.HARNESS,
        rain = Rain.NONE,
        temperature = Temperature.COOL,
        wind = Wind.BREEZE,
        snow = Snow.NONE,
        notes = "Barked at the bin lorry, otherwise steady. Off-lead in the field for ten minutes.",
        isLoading = false,
    )
}
