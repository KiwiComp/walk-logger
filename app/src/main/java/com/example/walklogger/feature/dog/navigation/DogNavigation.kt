package com.example.walklogger.feature.dog.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.feature.dog.DogRoute
import com.example.walklogger.feature.dog.registerdog.RegisterDogRoute
import kotlinx.serialization.Serializable

@Serializable
data object DogDestination : NavKey

/** With a [dogId] the form edits that dog instead of registering a new one. */
@Serializable
data class RegisterDogDestination(val dogId: Long? = null) : NavKey

fun MutableList<NavKey>.navigateToRegisterDog() {
    add(RegisterDogDestination())
}
fun MutableList<NavKey>.navigateToEditDog(dogId: Long) {
    add(RegisterDogDestination(dogId = dogId))
}

fun EntryProviderScope<NavKey>.dogScreen(
    onAddDog: () -> Unit,
    onEditDog: (dogId: Long) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<DogDestination> {
        DogRoute(onAddDog = onAddDog, onEditDog = onEditDog, snackbarHostState = snackbarHostState)
    }
}

fun EntryProviderScope<NavKey>.registerDogScreen(
    onDone: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<RegisterDogDestination> { key ->
        RegisterDogRoute(
            dogId = key.dogId,
            onDone = onDone,
            onNotFound = onNotFound,
            snackbarHostState = snackbarHostState,
        )
    }
}
