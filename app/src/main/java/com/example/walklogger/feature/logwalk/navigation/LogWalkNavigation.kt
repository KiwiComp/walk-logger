package com.example.walklogger.feature.logwalk.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.walklogger.feature.logwalk.NewWalkRoute
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** [epochDay] is the day chosen on the log screen, see [java.time.LocalDate.toEpochDay]. */
@Serializable
data class NewWalkDestination(val epochDay: Long)

fun NavController.navigateToNewWalk(date: LocalDate) = navigate(NewWalkDestination(epochDay = date.toEpochDay()))

fun NavGraphBuilder.newWalkScreen(onBack: () -> Unit) {
    composable<NewWalkDestination> { entry ->
        NewWalkRoute(
            date = LocalDate.ofEpochDay(entry.toRoute<NewWalkDestination>().epochDay),
            onBack = onBack,
        )
    }
}
