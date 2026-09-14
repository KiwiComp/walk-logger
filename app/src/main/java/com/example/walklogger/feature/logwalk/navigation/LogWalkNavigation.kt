package com.example.walklogger.feature.logwalk.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.walklogger.feature.logwalk.LogPastWalkRoute
import com.example.walklogger.feature.logwalk.NewWalkRoute
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** [epochDay] is the day chosen on the log screen, see [java.time.LocalDate.toEpochDay]. */
@Serializable
data class NewWalkDestination(val epochDay: Long)

/**
 * [epochDay] prefills the form's date; the user can still change it.
 * With a [walkId] the form edits that walk instead of logging a new one.
 */
@Serializable
data class LogPastWalkDestination(val epochDay: Long, val walkId: Long? = null)

fun NavController.navigateToNewWalk(date: LocalDate) = navigate(NewWalkDestination(epochDay = date.toEpochDay()))

fun NavController.navigateToLogPastWalk(date: LocalDate) =
    navigate(LogPastWalkDestination(epochDay = date.toEpochDay()))

fun NavController.navigateToEditWalk(date: LocalDate, walkId: Long) =
    navigate(LogPastWalkDestination(epochDay = date.toEpochDay(), walkId = walkId))

fun NavGraphBuilder.newWalkScreen(onBack: () -> Unit, onLogPastWalk: (LocalDate) -> Unit) {
    composable<NewWalkDestination> { entry ->
        NewWalkRoute(
            date = LocalDate.ofEpochDay(entry.toRoute<NewWalkDestination>().epochDay),
            onBack = onBack,
            onLogPastWalk = onLogPastWalk,
        )
    }
}

fun NavGraphBuilder.logPastWalkScreen(onClose: () -> Unit, onSaved: () -> Unit) {
    composable<LogPastWalkDestination> { entry ->
        val destination = entry.toRoute<LogPastWalkDestination>()
        LogPastWalkRoute(
            date = LocalDate.ofEpochDay(destination.epochDay),
            walkId = destination.walkId,
            onClose = onClose,
            onSaved = onSaved,
        )
    }
}
