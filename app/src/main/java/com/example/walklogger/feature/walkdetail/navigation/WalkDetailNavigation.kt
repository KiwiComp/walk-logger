package com.example.walklogger.feature.walkdetail.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.walklogger.feature.walkdetail.WalkDetailRoute
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** The read-only view of the logged walk [walkId]. */
@Serializable
data class WalkDetailDestination(val walkId: Long)

fun NavController.navigateToWalkDetail(walkId: Long) = navigate(WalkDetailDestination(walkId))

fun NavGraphBuilder.walkDetailScreen(
    onBack: () -> Unit,
    onEdit: (LocalDate, Long) -> Unit,
    onDiscarded: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    composable<WalkDetailDestination> { entry ->
        WalkDetailRoute(
            walkId = entry.toRoute<WalkDetailDestination>().walkId,
            onBack = onBack,
            onEdit = onEdit,
            onDiscarded = onDiscarded,
            onNotFound = onNotFound,
            snackbarHostState = snackbarHostState,
        )
    }
}
