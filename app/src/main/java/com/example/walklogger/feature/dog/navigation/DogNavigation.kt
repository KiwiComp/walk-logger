package com.example.walklogger.feature.dog.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.walklogger.feature.dog.DogRoute
import com.example.walklogger.feature.dog.registerdog.RegisterDogRoute
import kotlinx.serialization.Serializable


@Serializable
data object DogDestination

/** With a [dogId] the form edits that dog instead of registering a new one. */
@Serializable
data class RegisterDogDestination(val dogId: Long? = null)

fun NavController.navigateToDog(navOptions: NavOptions? = null) = navigate(DogDestination, navOptions)

fun NavController.navigateToRegisterDog() = navigate(RegisterDogDestination())
fun NavController.navigateToEditDog(dogId: Long) = navigate(RegisterDogDestination(dogId = dogId))


fun NavGraphBuilder.dogScreen(onAddDog: () -> Unit, onEditDog: (dogId: Long) -> Unit) {
    composable<DogDestination> { DogRoute(onAddDog = onAddDog, onEditDog = onEditDog) }
}

fun NavGraphBuilder.registerDogScreen(onDone: () -> Unit) {
    composable<RegisterDogDestination> { entry ->
        RegisterDogRoute(dogId = entry.toRoute<RegisterDogDestination>().dogId, onDone = onDone)
    }
}
