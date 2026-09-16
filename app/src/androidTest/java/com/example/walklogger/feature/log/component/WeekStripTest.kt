package com.example.walklogger.feature.log.component

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import androidx.test.platform.app.InstrumentationRegistry
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.log.DayUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    private val english: Locale = Locale.ENGLISH
    private val swedish: Locale = Locale.forLanguageTag("sv")

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

    /** Renders the strip under [locale] so the test doesn't depend on the device language. */
    private fun setStrip(
        days: List<DayUiState>,
        locale: Locale = english,
        onDaySelected: (LocalDate) -> Unit = {},
    ) {
        composeRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply { setLocale(locale) }
            val context = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalContext provides context) {
                WalkLoggerTheme { WeekStrip(days = days, onDaySelected = onDaySelected) }
            }
        }
    }

    private fun localized(locale: Locale): Context {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        return base.createConfigurationContext(Configuration(base.resources.configuration).apply { setLocale(locale) })
    }

    private fun LocalDate.description(locale: Locale = english): String =
        format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale))

    private fun day(date: LocalDate) = composeRule.onNodeWithContentDescription(date.description())

    private fun todayNode() = composeRule.onNodeWithContentDescription(
        localized(english).getString(R.string.day_today_description, today.description()),
    )

    @Test
    fun showsEveryDayOfTheWeek() {
        setStrip(week())

        (0L until 7L).map { monday.plusDays(it) }.filterNot { it == today }.forEach { date ->
            day(date).assertIsNotSelected()
            composeRule.onNode(hasText(date.dayOfMonth.toString())).assertExists()
        }
        todayNode().assertIsSelected()
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
        todayNode().assertIsNotSelected()
    }

    @Test
    fun describesWhetherADayHasWalks() {
        val tuesday = monday.plusDays(1)
        setStrip(week(withWalks = setOf(tuesday)))

        val strings = localized(english)
        day(tuesday).assert(hasStateDescription(strings.getString(R.string.day_has_walks)))
        day(monday).assert(hasStateDescription(strings.getString(R.string.day_no_walks)))
    }

    @Test
    fun followsTheConfiguredLocale() {
        val tuesday = monday.plusDays(1)
        setStrip(week(withWalks = setOf(tuesday)), locale = swedish)

        composeRule.onNodeWithContentDescription("Idag, lördag 12 september").assertIsSelected()
        composeRule.onNodeWithContentDescription("tisdag 8 september").assert(hasStateDescription("Har promenader"))
        composeRule.onNodeWithContentDescription("måndag 7 september").assert(hasStateDescription("Inga promenader"))
        composeRule.onNode(hasText("MÅN")).assertExists()
    }
}
