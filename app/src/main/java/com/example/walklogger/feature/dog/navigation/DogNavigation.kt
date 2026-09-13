package com.example.walklogger.feature.dog.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.walklogger.feature.dog.DogScreen
import kotlinx.serialization.Serializable

@Serializable
data object DogDestination

fun NavController.navigateToDog(navOptions: NavOptions? = null) = navigate(DogDestination, navOptions)

fun NavGraphBuilder.dogScreen() {
    composable<DogDestination> { DogScreen() }
}
