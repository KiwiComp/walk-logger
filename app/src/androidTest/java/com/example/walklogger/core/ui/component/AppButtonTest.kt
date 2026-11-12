package com.example.walklogger.core.ui.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val label = "Save walk"

    private fun setButton(
        enabled: Boolean = true,
        loading: Boolean = false,
        onClick: () -> Unit = {
        },
    ) {
        composeRule.setContent {
            WalkLoggerTheme {
                AppButton(text = label, onClick = onClick, enabled = enabled, loading = loading)
            }
        }
    }

    private fun button() = composeRule.onNodeWithText(label)

    private fun spinner() = composeRule.onNode(
        hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate),
        useUnmergedTree = true,
    )

    @Test
    fun showsLabelAndReportsClicks() {
        var clicks = 0
        setButton(onClick = { clicks++ })

        button().assertIsEnabled().performClick()

        assertEquals(1, clicks)
        spinner().assertDoesNotExist()
    }

    @Test
    fun everyStyleShowsLabelAndReportsClicks() {
        var style by mutableStateOf(AppButtonStyle.Primary)
        var clicks = 0
        composeRule.setContent {
            WalkLoggerTheme { AppButton(text = label, onClick = { clicks++ }, style = style) }
        }

        AppButtonStyle.entries.forEach { entry ->
            composeRule.runOnIdle { style = entry }
            clicks = 0

            button().assertIsEnabled().performClick()

            assertEquals("style $entry", 1, clicks)
        }
    }

    @Test
    fun disabledButtonIgnoresClicks() {
        var clicks = 0
        setButton(enabled = false, onClick = { clicks++ })

        button().assertIsNotEnabled().performClick()

        assertEquals(0, clicks)
    }

    @Test
    fun loadingShowsSpinnerAndIgnoresClicks() {
        var clicks = 0
        setButton(loading = true, onClick = { clicks++ })

        spinner().assertExists()
        button().assertIsEnabled().performClick()

        assertEquals(0, clicks)
    }
}
