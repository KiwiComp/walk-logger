package com.example.walklogger.feature.calendar.preview

import com.example.walklogger.core.model.Equipment
import com.example.walklogger.feature.calendar.CalendarDayUiState
import com.example.walklogger.feature.calendar.CalendarUiState
import com.example.walklogger.feature.calendar.CalendarWalkUiState
import java.time.LocalTime
import java.time.YearMonth


/** Fixed content for `@Preview`s only; never reaches the running app. */
internal object CalendarPreviewData {
    private val month = YearMonth.of(2026, 9)
    private val today = month.atDay(12)

    /** Walk count and rounded rating per day of the month; days missing here have no walks. */
    private val loggedDays = mapOf(
        1 to (1 to 5), 2 to (1 to 6), 3 to (2 to 2), 4 to (1 to 5), 5 to (1 to 6), 6 to (2 to 1),
        7 to (2 to 4), 8 to (1 to 5), 10 to (2 to 4), 11 to (1 to 5), 12 to (2 to 7),
    )

    val walks = listOf(
        CalendarWalkUiState(
            id = 1,
            startTime = LocalTime.of(7, 5),
            equipment = Equipment.HARNESS,
            rating = 6,
            note = "Long morning loop, calm the whole way.",
        ),
        CalendarWalkUiState(
            id = 2,
            startTime = LocalTime.of(11, 30),
            equipment = Equipment.COLLAR,
            rating = 4,
            note = "",
        ),
    )

    val uiState = CalendarUiState(
        month = month,
        selectedDate = today,
        days = (1..month.lengthOfMonth()).map { dayOfMonth ->
            val (walkCount, rating) = loggedDays[dayOfMonth] ?: (0 to null)
            CalendarDayUiState(
                date = month.atDay(dayOfMonth),
                walkCount = walkCount,
                rating = rating,
                isSelected = dayOfMonth == today.dayOfMonth,
                isEnabled = dayOfMonth <= today.dayOfMonth,
            )
        },
        walks = walks,
        isCurrentMonth = true,
        canStartLiveWalk = true,
        activeDogName = "Sixten",
    )
}
