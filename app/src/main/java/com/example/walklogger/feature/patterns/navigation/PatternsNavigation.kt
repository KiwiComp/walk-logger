package com.example.walklogger.feature.patterns.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.walklogger.feature.patterns.PatternsRoute
import kotlinx.serialization.Serializable

@Serializable
data object PatternsDestination

fun NavController.navigateToPatterns(navOptions: NavOptions? = null) =
    navigate(PatternsDestination, navOptions)

fun NavGraphBuilder.patternsScreen() {
    composable<PatternsDestination> { PatternsRoute() }
}
