package com.example.walklogger.feature.walkdetail.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.feature.walkdetail.WalkDetailRoute
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** The read-only view of the logged walk [walkId]. */
@Serializable
data class WalkDetailDestination(val walkId: Long) : NavKey

fun MutableList<NavKey>.navigateToWalkDetail(walkId: Long) {
    add(WalkDetailDestination(walkId))
}

fun EntryProviderScope<NavKey>.walkDetailScreen(
    onBack: () -> Unit,
    onEdit: (LocalDate, Long) -> Unit,
    onDiscarded: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<WalkDetailDestination> { key ->
        WalkDetailRoute(
            walkId = key.walkId,
            onBack = onBack,
            onEdit = onEdit,
            onDiscarded = onDiscarded,
            onNotFound = onNotFound,
            snackbarHostState = snackbarHostState,
        )
    }
}
