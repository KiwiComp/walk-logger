package com.example.walklogger.feature.calendar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.walklogger.feature.calendar.CalendarRoute
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object CalendarDestination

fun NavController.navigateToCalendar(navOptions: NavOptions? = null) =
    navigate(CalendarDestination, navOptions)

fun NavGraphBuilder.calendarScreen(
    onAddWalk: (LocalDate) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    onOpenWalk: (Long) -> Unit,
    onGoToDogs: () -> Unit,
) {
    composable<CalendarDestination> {
        CalendarRoute(
            onAddWalk = onAddWalk,
            onLogPastWalk = onLogPastWalk,
            onOpenWalk = onOpenWalk,
            onGoToDogs = onGoToDogs,
        )
    }
}
