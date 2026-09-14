package com.example.walklogger.feature.log

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeekStripTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val monday = LocalDate.of(2026, 9, 7)
    private val today = LocalDate.of(2026, 9, 12)
    private val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM")

    private fun week(selected: LocalDate = today, withWalks: Set<LocalDate> = emptySet()) =
        (0L until 7L).map { monday.plusDays(it) }.map { day ->
            DayUiState(
                date = day,
                hasWalks = day in withWalks,
                isSelected = day == selected,
                isToday = day == today,
                isEnabled = !day.isAfter(today),
            )
        }

    private fun setStrip(days: List<DayUiState>, onDaySelected: (LocalDate) -> Unit = {}) {
        composeRule.setContent {
            WalkLoggerTheme { WeekStrip(days = days, onDaySelected = onDaySelected) }
        }
    }

    private fun day(date: LocalDate) = composeRule.onNodeWithContentDescription(date.format(formatter))

    @Test
    fun showsEveryDayOfTheWeek() {
        setStrip(week())

        (0L until 7L).map { monday.plusDays(it) }.filterNot { it == today }.forEach { date ->
            day(date).assertIsNotSelected()
            composeRule.onNode(hasText(date.dayOfMonth.toString())).assertExists()
        }
        composeRule.onNodeWithContentDescription("Today, ${today.format(formatter)}").assertIsSelected()
    }

    @Test
    fun todayShowsNoLabelOrNumber() {
        setStrip(week())

        composeRule.onNode(hasText(today.dayOfMonth.toString())).assertDoesNotExist()
    }

    @Test
    fun futureDaysAreDisabledAndIgnoreTaps() {
        val sunday = today.plusDays(1)
        var selected: LocalDate? = null
        setStrip(week(), onDaySelected = { selected = it })

        day(sunday).assertIsNotEnabled().performClick()

        assertEquals(null, selected)
        day(monday).assertIsEnabled()
    }

    @Test
    fun tappingADayReportsItsDate() {
        val friday = today.minusDays(1)
        var selected: LocalDate? = null
        setStrip(week(), onDaySelected = { selected = it })

        day(friday).performClick()

        assertEquals(friday, selected)
    }

    @Test
    fun reflectsSelectionFromState() {
        val friday = today.minusDays(1)
        setStrip(week(selected = friday))

        day(friday).assertIsSelected()
        composeRule.onNodeWithContentDescription("Today, ${today.format(formatter)}").assertIsNotSelected()
    }

    @Test
    fun describesWhetherADayHasWalks() {
        val tuesday = monday.plusDays(1)
        setStrip(week(withWalks = setOf(tuesday)))

        day(tuesday).assert(hasStateDescription("Has walks"))
        day(monday).assert(hasStateDescription("No walks"))
    }
}
