package com.example.walklogger.feature.calendar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.walklogger.feature.calendar.CalendarRoute
import kotlinx.serialization.Serializable

@Serializable
data object CalendarDestination

fun NavController.navigateToCalendar(navOptions: NavOptions? = null) =
    navigate(CalendarDestination, navOptions)

fun NavGraphBuilder.calendarScreen() {
    composable<CalendarDestination> { CalendarRoute() }
}
