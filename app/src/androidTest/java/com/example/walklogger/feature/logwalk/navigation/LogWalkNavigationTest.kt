package com.example.walklogger.feature.logwalk.navigation

import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.walklogger.feature.log.navigation.LogDestination
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the log → new walk → log past walk stack without composing any screens. */
@RunWith(AndroidJUnit4::class)
class LogWalkNavigationTest {

    private val date = LocalDate.of(2026, 9, 12)
    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() = onMain {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            graph = createGraph(startDestination = LogDestination) {
                composable<LogDestination> {}
                composable<NewWalkDestination> {}
                composable<LogPastWalkDestination> {}
            }
        }
    }

    @Test
    fun navigateToLogPastWalk_carriesTheChosenDay() = onMain {
        navController.navigateToNewWalk(date)
        navController.navigateToLogPastWalk(date)

        val entry = navController.currentBackStackEntry!!
        assertTrue(entry.destination.hasRoute<LogPastWalkDestination>())
        assertEquals(date, LocalDate.ofEpochDay(entry.toRoute<LogPastWalkDestination>().epochDay))
    }

    @Test
    fun closingLogPastWalk_returnsToNewWalk() = onMain {
        navController.navigateToNewWalk(date)
        navController.navigateToLogPastWalk(date)

        navController.popBackStack()

        assertTrue(navController.currentDestination!!.hasRoute<NewWalkDestination>())
    }

    @Test
    fun savingLogPastWalk_returnsToLogAndDropsNewWalk() = onMain {
        navController.navigateToNewWalk(date)
        navController.navigateToLogPastWalk(date)

        navController.popBackStack<LogDestination>(inclusive = false)

        assertTrue(navController.currentDestination!!.hasRoute<LogDestination>())
        assertTrue(navController.currentBackStack.value.none { it.destination.hasRoute<NewWalkDestination>() })
    }

    private fun onMain(block: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
}
