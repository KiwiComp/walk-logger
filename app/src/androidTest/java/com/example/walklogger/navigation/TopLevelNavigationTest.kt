package com.example.walklogger.navigation

import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.walklogger.feature.calendar.navigation.CalendarDestination
import com.example.walklogger.feature.dog.navigation.DogDestination
import com.example.walklogger.feature.dog.navigation.RegisterDogDestination
import com.example.walklogger.feature.dog.navigation.navigateToRegisterDog
import com.example.walklogger.feature.log.navigation.LogDestination
import com.example.walklogger.feature.patterns.navigation.PatternsDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises [navigateToTopLevel] against the app's destinations without composing any screens. */
@RunWith(AndroidJUnit4::class)
class TopLevelNavigationTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() = onMain {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            graph = createGraph(startDestination = LogDestination) {
                composable<LogDestination> {}
                composable<CalendarDestination> {}
                composable<PatternsDestination> {}
                composable<DogDestination> {}
                composable<RegisterDogDestination> {}
            }
        }
    }

    @Test
    fun switchingTabs_keepsOneEntryPerTab() = onMain {
        navController.navigateToTopLevel(TopLevelDestination.DOG)
        navController.navigateToTopLevel(TopLevelDestination.CALENDAR)
        navController.navigateToTopLevel(TopLevelDestination.DOG)

        assertTrue(navController.currentDestination!!.hasRoute<DogDestination>())
        assertEquals(2, navController.currentBackStack.value.count { it.destination.route != null })
    }

    @Test
    fun leavingRegisterDog_dropsItFromTheDogsTab() = onMain {
        navController.navigateToTopLevel(TopLevelDestination.DOG)
        navController.navigateToRegisterDog()
        assertTrue(navController.currentDestination!!.hasRoute<RegisterDogDestination>())

        navController.navigateToTopLevel(TopLevelDestination.LOG)
        navController.navigateToTopLevel(TopLevelDestination.DOG)

        assertTrue(navController.currentDestination!!.hasRoute<DogDestination>())
        assertTrue(
            navController.currentBackStack.value.none {
                it.destination.hasRoute<RegisterDogDestination>()
            },
        )
    }

    @Test
    fun reselectingDogsTab_fromRegisterDog_returnsToDogScreen() = onMain {
        navController.navigateToTopLevel(TopLevelDestination.DOG)
        navController.navigateToRegisterDog()

        navController.navigateToTopLevel(TopLevelDestination.DOG)

        assertTrue(navController.currentDestination!!.hasRoute<DogDestination>())
    }

    private fun onMain(block: () -> Unit) =
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
}
