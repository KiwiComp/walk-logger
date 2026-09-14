package com.example.walklogger.feature.dog.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.walklogger.feature.dog.DogRoute
import com.example.walklogger.feature.dog.RegisterDogRoute
import kotlinx.serialization.Serializable

@Serializable
data object DogDestination

@Serializable
data object RegisterDogDestination

fun NavController.navigateToDog(navOptions: NavOptions? = null) = navigate(DogDestination, navOptions)

fun NavController.navigateToRegisterDog() = navigate(RegisterDogDestination)

fun NavGraphBuilder.dogScreen(onAddDog: () -> Unit) {
    composable<DogDestination> { DogRoute(onAddDog = onAddDog) }
}

fun NavGraphBuilder.registerDogScreen(onDone: () -> Unit) {
    composable<RegisterDogDestination> { RegisterDogRoute(onDone = onDone) }
}
