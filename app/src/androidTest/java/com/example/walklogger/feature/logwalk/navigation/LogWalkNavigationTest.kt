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
import com.example.walklogger.feature.walkdetail.navigation.WalkDetailDestination
import com.example.walklogger.feature.walkdetail.navigation.navigateToWalkDetail
import com.example.walklogger.navigation.popToTopLevel
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the log → new walk → log past walk / timer → after-walk stacks without composing any screens. */
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
                composable<ActiveWalkDestination> {}
                composable<FinishWalkDestination> {}
                composable<WalkDetailDestination> {}
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

    @Test
    fun startingAWalk_replacesNewWalkWithTheTimer() = onMain {
        navController.navigateToNewWalk(date)

        navController.navigateToActiveWalk(walkId = 7)

        val entry = navController.currentBackStackEntry!!
        assertTrue(entry.destination.hasRoute<ActiveWalkDestination>())
        assertEquals(7L, entry.toRoute<ActiveWalkDestination>().walkId)
        assertTrue(navController.currentBackStack.value.none { it.destination.hasRoute<NewWalkDestination>() })
    }

    @Test
    fun resumingTheTimerTwice_keepsOneEntry() = onMain {
        navController.navigateToActiveWalk(walkId = 7)
        navController.navigateToActiveWalk(walkId = 7)

        assertEquals(1, navController.currentBackStack.value.count { it.destination.hasRoute<ActiveWalkDestination>() })
    }

    @Test
    fun endingAWalk_replacesTheTimerWithTheForm() = onMain {
        navController.navigateToActiveWalk(walkId = 7)

        navController.navigateToFinishWalk(walkId = 7)

        val entry = navController.currentBackStackEntry!!
        assertTrue(entry.destination.hasRoute<FinishWalkDestination>())
        assertEquals(7L, entry.toRoute<FinishWalkDestination>().walkId)
        assertTrue(navController.currentBackStack.value.none { it.destination.hasRoute<ActiveWalkDestination>() })
        assertTrue(navController.previousBackStackEntry!!.destination.hasRoute<LogDestination>())
    }

    @Test
    fun editingAMissingWalk_returnsToTheTabPastItsDetail() = onMain {
        navController.navigateToWalkDetail(walkId = 7)
        navController.navigateToEditWalk(date, walkId = 7)

        navController.popToTopLevel()

        assertTrue(navController.currentDestination!!.hasRoute<LogDestination>())
        assertTrue(navController.currentBackStack.value.none { it.destination.hasRoute<WalkDetailDestination>() })
    }

    private fun onMain(block: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
}
