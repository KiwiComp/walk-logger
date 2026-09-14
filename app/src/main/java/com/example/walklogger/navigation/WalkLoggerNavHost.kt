package com.example.walklogger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.walklogger.feature.calendar.navigation.calendarScreen
import com.example.walklogger.feature.dog.navigation.dogScreen
import com.example.walklogger.feature.dog.navigation.navigateToRegisterDog
import com.example.walklogger.feature.dog.navigation.registerDogScreen
import com.example.walklogger.feature.log.navigation.LogDestination
import com.example.walklogger.feature.log.navigation.logScreen
import com.example.walklogger.feature.logwalk.navigation.navigateToNewWalk
import com.example.walklogger.feature.logwalk.navigation.newWalkScreen
import com.example.walklogger.feature.patterns.navigation.patternsScreen

/** App-level graph. Features contribute their destinations via extension functions. */
@Composable
fun WalkLoggerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LogDestination,
        modifier = modifier,
    ) {
        logScreen(
            onAddWalk = navController::navigateToNewWalk,
            onGoToDogs = { navController.navigateToTopLevel(TopLevelDestination.DOG) },
        )
        calendarScreen()
        patternsScreen()
        dogScreen(onAddDog = navController::navigateToRegisterDog)
        registerDogScreen(onDone = { navController.popBackStack() })
        newWalkScreen(onBack = { navController.popBackStack() })
    }
}
