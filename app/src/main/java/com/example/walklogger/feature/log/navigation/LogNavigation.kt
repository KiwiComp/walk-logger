package com.example.walklogger.feature.log.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.walklogger.feature.log.LogScreen
import kotlinx.serialization.Serializable

@Serializable
data object LogDestination

fun NavController.navigateToLog(navOptions: NavOptions? = null) = navigate(LogDestination, navOptions)

fun NavGraphBuilder.logScreen() {
    composable<LogDestination> { LogScreen() }
}
