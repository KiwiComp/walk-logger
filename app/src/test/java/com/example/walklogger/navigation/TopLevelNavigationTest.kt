package com.example.walklogger.navigation

import androidx.compose.runtime.mutableStateOf
import com.example.walklogger.feature.calendar.navigation.CalendarDestination
import com.example.walklogger.feature.dog.navigation.DogDestination
import com.example.walklogger.feature.dog.navigation.navigateToRegisterDog
import com.example.walklogger.feature.log.navigation.LogDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelNavigationTest {

    private val state = WalkLoggerNavigationState(
        currentTab = mutableStateOf(TopLevelDestination.LOG),
        backStacks = TopLevelDestination.entries.associateWith { mutableListOf(it.key) },
    )

    @Test
    fun `back from another tab's root returns to the log tab`() {
        state.selectTab(TopLevelDestination.CALENDAR)
        assertEquals(listOf(CalendarDestination), state.currentStack)

        state.goBack()

        assertEquals(TopLevelDestination.LOG, state.currentTab)
        assertEquals(listOf(LogDestination), state.currentStack)
    }

    @Test
    fun `leaving register dog drops it from the dogs tab`() {
        state.selectTab(TopLevelDestination.DOG)
        state.currentStack.navigateToRegisterDog()

        state.selectTab(TopLevelDestination.LOG)
        state.selectTab(TopLevelDestination.DOG)

        assertEquals(listOf(DogDestination), state.currentStack)
    }

    @Test
    fun `reselecting the dogs tab from register dog returns to the dog screen`() {
        state.selectTab(TopLevelDestination.DOG)
        state.currentStack.navigateToRegisterDog()

        state.selectTab(TopLevelDestination.DOG)

        assertEquals(listOf(DogDestination), state.currentStack)
    }
}
