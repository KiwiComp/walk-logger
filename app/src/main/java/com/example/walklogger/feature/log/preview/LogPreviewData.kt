package com.example.walklogger.feature.log.preview

import com.example.walklogger.core.model.Equipment
import com.example.walklogger.feature.log.DayUiState
import com.example.walklogger.feature.log.LogUiState
import com.example.walklogger.feature.log.WalkUiState
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


/** Fixed content for `@Preview`s only; never reaches the running app. */
internal object LogPreviewData {
    private val saturday = LocalDate.of(2026, 9, 12)

    val walk = WalkUiState(
        id = 1,
        startTime = LocalTime.of(7, 5),
        duration = Duration.ofMinutes(38),
        equipment = Equipment.HARNESS,
        rating = 6,
        note = "Long morning loop, calm the whole way.",
    )

    val walks = listOf(
        walk,
        WalkUiState(
            id = 2,
            startTime = LocalTime.of(11, 30),
            duration = Duration.ofMinutes(17),
            equipment = Equipment.COLLAR,
            rating = 4,
            note = "Quick errand walk, distracted by the market.",
        ),
    )

    val uiState = LogUiState(
        selectedDate = saturday,
        week = (0L until 7L).map { offset ->
            val day = saturday.minusDays(5).plusDays(offset)
            DayUiState(
                date = day,
                hasWalks = offset !in setOf(2L, 6L),
                isSelected = day == saturday,
                isToday = day == saturday,
                isEnabled = !day.isAfter(saturday),
            )
        },
        walks = walks,
        canStartLiveWalk = true,
        activeDogName = "Sixten",
    )
}
