package com.example.walklogger.feature.log.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.feature.log.LogRoute
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object LogDestination : NavKey

fun EntryProviderScope<NavKey>.logScreen(
    onAddWalk: (LocalDate) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    onOpenWalk: (Long) -> Unit,
    onGoToDogs: () -> Unit,
    onOpenSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<LogDestination> {
        LogRoute(
            onAddWalk = onAddWalk,
            onLogPastWalk = onLogPastWalk,
            onOpenWalk = onOpenWalk,
            onGoToDogs = onGoToDogs,
            onOpenSettings = onOpenSettings,
            snackbarHostState = snackbarHostState,
        )
    }
}
