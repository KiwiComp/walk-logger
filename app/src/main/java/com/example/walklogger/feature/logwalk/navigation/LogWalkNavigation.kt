package com.example.walklogger.feature.logwalk.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.feature.logwalk.activewalk.ActiveWalkRoute
import com.example.walklogger.feature.logwalk.finishwalk.FinishWalkRoute
import com.example.walklogger.feature.logwalk.newwalk.NewWalkRoute
import com.example.walklogger.feature.logwalk.pastwalk.LogPastWalkRoute
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** [epochDay] is the day chosen on the log screen, see [java.time.LocalDate.toEpochDay]. */
@Serializable
data class NewWalkDestination(val epochDay: Long) : NavKey

/**
 * [epochDay] prefills the form's date; the user can still change it.
 * With a [walkId] the form edits that walk instead of logging a new one.
 */
@Serializable
data class LogPastWalkDestination(val epochDay: Long, val walkId: Long? = null) : NavKey

/** The timer for the in-progress walk [walkId]. */
@Serializable
data class ActiveWalkDestination(val walkId: Long) : NavKey

/** The after-walk form for the just-finished walk [walkId]. */
@Serializable
data class FinishWalkDestination(val walkId: Long) : NavKey

fun MutableList<NavKey>.navigateToNewWalk(date: LocalDate) {
    add(NewWalkDestination(epochDay = date.toEpochDay()))
}

/** Replaces the new-walk form, so back from the timer never lands on it. */
fun MutableList<NavKey>.navigateToActiveWalk(walkId: Long) {
    popUpToInclusive<NewWalkDestination>()
    val destination = ActiveWalkDestination(walkId)
    if (lastOrNull() != destination) add(destination)
}

/** Replaces the timer screen; the walk is already finished so there is nothing to go back to. */
fun MutableList<NavKey>.navigateToFinishWalk(walkId: Long) {
    popUpToInclusive<ActiveWalkDestination>()
    add(FinishWalkDestination(walkId))
}

fun MutableList<NavKey>.navigateToLogPastWalk(date: LocalDate) {
    add(LogPastWalkDestination(epochDay = date.toEpochDay()))
}

fun MutableList<NavKey>.navigateToEditWalk(date: LocalDate, walkId: Long) {
    add(LogPastWalkDestination(epochDay = date.toEpochDay(), walkId = walkId))
}

/** Nav2's `popUpTo<T> { inclusive = true }`: drops the topmost [T] and everything above it. */
private inline fun <reified T : NavKey> MutableList<NavKey>.popUpToInclusive() {
    val index = indexOfLast { it is T }
    if (index == -1) return
    while (size > index) removeAt(lastIndex)
}

fun EntryProviderScope<NavKey>.newWalkScreen(
    onBack: () -> Unit,
    onWalkStarted: (walkId: Long) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<NewWalkDestination> { key ->
        NewWalkRoute(
            date = LocalDate.ofEpochDay(key.epochDay),
            onBack = onBack,
            onWalkStarted = onWalkStarted,
            onLogPastWalk = onLogPastWalk,
            snackbarHostState = snackbarHostState,
        )
    }
}

fun EntryProviderScope<NavKey>.activeWalkScreen(
    onWalkEnded: (walkId: Long) -> Unit,
    onWalkDiscarded: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<ActiveWalkDestination> { key ->
        ActiveWalkRoute(
            walkId = key.walkId,
            onWalkEnded = onWalkEnded,
            onWalkDiscarded = onWalkDiscarded,
            onNotFound = onNotFound,
            snackbarHostState = snackbarHostState,
        )
    }
}

fun EntryProviderScope<NavKey>.finishWalkScreen(
    onSaved: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<FinishWalkDestination> { key ->
        FinishWalkRoute(
            walkId = key.walkId,
            onSaved = onSaved,
            onNotFound = onNotFound,
            snackbarHostState = snackbarHostState,
        )
    }
}

/** [onLogged] runs after a new walk is stored, [onUpdated] after an edited one. */
fun EntryProviderScope<NavKey>.logPastWalkScreen(
    onClose: () -> Unit,
    onLogged: () -> Unit,
    onUpdated: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<LogPastWalkDestination> { key ->
        LogPastWalkRoute(
            date = LocalDate.ofEpochDay(key.epochDay),
            walkId = key.walkId,
            onClose = onClose,
            onSaved = if (key.walkId == null) onLogged else onUpdated,
            onNotFound = onNotFound,
            snackbarHostState = snackbarHostState,
        )
    }
}
