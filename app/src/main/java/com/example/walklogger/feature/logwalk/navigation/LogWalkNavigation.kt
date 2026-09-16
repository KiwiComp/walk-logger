package com.example.walklogger.feature.logwalk.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.walklogger.feature.logwalk.activewalk.ActiveWalkRoute
import com.example.walklogger.feature.logwalk.finishwalk.FinishWalkRoute
import com.example.walklogger.feature.logwalk.newwalk.NewWalkRoute
import com.example.walklogger.feature.logwalk.pastwalk.LogPastWalkRoute
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

/** The timer for the in-progress walk [walkId]. */
@Serializable
data class ActiveWalkDestination(val walkId: Long)

/** The after-walk form for the just-finished walk [walkId]. */
@Serializable
data class FinishWalkDestination(val walkId: Long)

fun NavController.navigateToNewWalk(date: LocalDate) = navigate(NewWalkDestination(epochDay = date.toEpochDay()))

/** Replaces the new-walk form, so back from the timer never lands on it. */
fun NavController.navigateToActiveWalk(walkId: Long) = navigate(ActiveWalkDestination(walkId)) {
    popUpTo<NewWalkDestination> { inclusive = true }
    launchSingleTop = true
}

/** Replaces the timer screen; the walk is already finished so there is nothing to go back to. */
fun NavController.navigateToFinishWalk(walkId: Long) = navigate(FinishWalkDestination(walkId)) {
    popUpTo<ActiveWalkDestination> { inclusive = true }
}

fun NavController.navigateToLogPastWalk(date: LocalDate) =
    navigate(LogPastWalkDestination(epochDay = date.toEpochDay()))

fun NavController.navigateToEditWalk(date: LocalDate, walkId: Long) =
    navigate(LogPastWalkDestination(epochDay = date.toEpochDay(), walkId = walkId))

fun NavGraphBuilder.newWalkScreen(
    onBack: () -> Unit,
    onWalkStarted: (walkId: Long) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
) {
    composable<NewWalkDestination> { entry ->
        NewWalkRoute(
            date = LocalDate.ofEpochDay(entry.toRoute<NewWalkDestination>().epochDay),
            onBack = onBack,
            onWalkStarted = onWalkStarted,
            onLogPastWalk = onLogPastWalk,
        )
    }
}

fun NavGraphBuilder.activeWalkScreen(onWalkEnded: (walkId: Long) -> Unit, onWalkDiscarded: () -> Unit) {
    composable<ActiveWalkDestination> { entry ->
        ActiveWalkRoute(
            walkId = entry.toRoute<ActiveWalkDestination>().walkId,
            onWalkEnded = onWalkEnded,
            onWalkDiscarded = onWalkDiscarded,
        )
    }
}

fun NavGraphBuilder.finishWalkScreen(onSaved: () -> Unit) {
    composable<FinishWalkDestination> { entry ->
        FinishWalkRoute(walkId = entry.toRoute<FinishWalkDestination>().walkId, onSaved = onSaved)
    }
}

/** [onLogged] runs after a new walk is stored, [onUpdated] after an edited one. */
fun NavGraphBuilder.logPastWalkScreen(onClose: () -> Unit, onLogged: () -> Unit, onUpdated: () -> Unit) {
    composable<LogPastWalkDestination> { entry ->
        val destination = entry.toRoute<LogPastWalkDestination>()
        LogPastWalkRoute(
            date = LocalDate.ofEpochDay(destination.epochDay),
            walkId = destination.walkId,
            onClose = onClose,
            onSaved = if (destination.walkId == null) onLogged else onUpdated,
        )
    }
}
