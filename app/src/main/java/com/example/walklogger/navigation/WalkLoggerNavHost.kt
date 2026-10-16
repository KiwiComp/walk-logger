package com.example.walklogger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.walklogger.feature.calendar.navigation.calendarScreen
import com.example.walklogger.feature.dog.navigation.dogScreen
import com.example.walklogger.feature.dog.navigation.navigateToEditDog
import com.example.walklogger.feature.dog.navigation.navigateToRegisterDog
import com.example.walklogger.feature.dog.navigation.registerDogScreen
import com.example.walklogger.feature.log.navigation.LogDestination
import com.example.walklogger.feature.log.navigation.logScreen
import com.example.walklogger.feature.logwalk.navigation.activeWalkScreen
import com.example.walklogger.feature.logwalk.navigation.finishWalkScreen
import com.example.walklogger.feature.logwalk.navigation.logPastWalkScreen
import com.example.walklogger.feature.logwalk.navigation.navigateToActiveWalk
import com.example.walklogger.feature.logwalk.navigation.navigateToEditWalk
import com.example.walklogger.feature.logwalk.navigation.navigateToFinishWalk
import com.example.walklogger.feature.logwalk.navigation.navigateToLogPastWalk
import com.example.walklogger.feature.logwalk.navigation.navigateToNewWalk
import com.example.walklogger.feature.logwalk.navigation.newWalkScreen
import com.example.walklogger.feature.patterns.navigation.patternsScreen
import com.example.walklogger.feature.walkdetail.navigation.navigateToWalkDetail
import com.example.walklogger.feature.walkdetail.navigation.walkDetailScreen

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
            onLogPastWalk = navController::navigateToLogPastWalk,
            onOpenWalk = navController::navigateToWalkDetail,
            onGoToDogs = { navController.navigateToTopLevel(TopLevelDestination.DOG) },
        )
        calendarScreen(
            onAddWalk = navController::navigateToNewWalk,
            onLogPastWalk = navController::navigateToLogPastWalk,
            onOpenWalk = navController::navigateToWalkDetail,
            onGoToDogs = { navController.navigateToTopLevel(TopLevelDestination.DOG) },
        )
        patternsScreen()
        dogScreen(
            onAddDog = navController::navigateToRegisterDog,
            onEditDog = navController::navigateToEditDog,
        )
        registerDogScreen(onDone = { navController.popBackStack() })
        newWalkScreen(
            onBack = { navController.popBackStack() },
            onWalkStarted = navController::navigateToActiveWalk,
            onLogPastWalk = navController::navigateToLogPastWalk,
        )
        // Walk flows can start from the Log or Calendar tab; finishing one returns to whichever it was.
        activeWalkScreen(
            onWalkEnded = navController::navigateToFinishWalk,
            onWalkDiscarded = navController::popToTopLevel,
        )
        finishWalkScreen(onSaved = navController::popToTopLevel)
        // Editing is pushed on top of the walk's detail screen, so every way out of the form lands back there.
        logPastWalkScreen(
            onClose = { navController.popBackStack() },
            onLogged = navController::popToTopLevel,
            onUpdated = { navController.popBackStack() },
        )
        walkDetailScreen(
            onBack = navController::popToTopLevel,
            onEdit = navController::navigateToEditWalk,
            onDiscarded = navController::popToTopLevel,
        )
    }
}
