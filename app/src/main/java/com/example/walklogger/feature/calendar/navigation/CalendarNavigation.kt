package com.example.walklogger.feature.calendar.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.feature.calendar.CalendarRoute
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object CalendarDestination : NavKey

fun EntryProviderScope<NavKey>.calendarScreen(
    onAddWalk: (LocalDate) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    onOpenWalk: (Long) -> Unit,
    onGoToDogs: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<CalendarDestination> {
        CalendarRoute(
            onAddWalk = onAddWalk,
            onLogPastWalk = onLogPastWalk,
            onOpenWalk = onOpenWalk,
            onGoToDogs = onGoToDogs,
            snackbarHostState = snackbarHostState,
        )
    }
}
