package com.example.walklogger.feature.logwalk.navigation

import androidx.compose.runtime.mutableStateOf
import com.example.walklogger.feature.log.navigation.LogDestination
import com.example.walklogger.feature.walkdetail.navigation.navigateToWalkDetail
import com.example.walklogger.navigation.TopLevelDestination
import com.example.walklogger.navigation.WalkLoggerNavigationState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class LogWalkNavigationTest {

    private val date = LocalDate.of(2026, 9, 12)
    private val state = WalkLoggerNavigationState(
        currentTab = mutableStateOf(TopLevelDestination.LOG),
        backStacks = TopLevelDestination.entries.associateWith { mutableListOf(it.key) },
    )
    private val backStack get() = state.currentStack

    @Test
    fun `log past walk carries the chosen day`() {
        backStack.navigateToNewWalk(date)
        backStack.navigateToLogPastWalk(date)

        assertEquals(LogPastWalkDestination(epochDay = date.toEpochDay()), backStack.last())
    }

    @Test
    fun `starting a walk replaces new walk with the timer`() {
        backStack.navigateToNewWalk(date)

        backStack.navigateToActiveWalk(walkId = 7)

        assertEquals(listOf(LogDestination, ActiveWalkDestination(7)), backStack)
    }

    @Test
    fun `resuming the timer twice keeps one entry`() {
        backStack.navigateToActiveWalk(walkId = 7)
        backStack.navigateToActiveWalk(walkId = 7)

        assertEquals(listOf(LogDestination, ActiveWalkDestination(7)), backStack)
    }

    @Test
    fun `ending a walk replaces the timer with the form`() {
        backStack.navigateToActiveWalk(walkId = 7)

        backStack.navigateToFinishWalk(walkId = 7)

        assertEquals(listOf(LogDestination, FinishWalkDestination(7)), backStack)
    }

    @Test
    fun `editing a missing walk returns to the tab past its detail`() {
        backStack.navigateToWalkDetail(walkId = 7)
        backStack.navigateToEditWalk(date, walkId = 7)

        state.popToTopLevel()

        assertEquals(listOf(LogDestination), backStack)
    }
}
