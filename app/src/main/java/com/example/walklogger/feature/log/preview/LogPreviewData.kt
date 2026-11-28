package com.example.walklogger.feature.log.preview

import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.ui.component.TimelineWalk
import com.example.walklogger.feature.log.DayUiState
import com.example.walklogger.feature.log.LogUiState
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes

/** Fixed content for `@Preview`s only; never reaches the running app. */
internal object LogPreviewData {
    private val saturday = LocalDate.of(2026, 9, 12)

    val walk = TimelineWalk(
        id = 1,
        startTime = LocalTime.of(7, 5),
        duration = 38.minutes,
        distanceMeters = 3_240.0,
        equipment = Equipment.HARNESS,
        rating = 6,
        note = "Long morning loop, calm the whole way.",
        hasPassedStool = null,
    )

    val walks = listOf(
        walk,
        TimelineWalk(
            id = 2,
            startTime = LocalTime.of(11, 30),
            duration = 177.minutes,
            distanceMeters = null,
            equipment = Equipment.COLLAR,
            rating = 4,
            note = "Quick errand walk, distracted by the market.",
            hasPassedStool = true,
        ),
    )

    val uiState = LogUiState(
        selectedDate = saturday,
        weekNumber = 37,
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
