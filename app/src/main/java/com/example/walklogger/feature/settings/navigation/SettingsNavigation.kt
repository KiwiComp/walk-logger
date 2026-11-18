package com.example.walklogger.feature.settings.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsDestination : NavKey

fun MutableList<NavKey>.navigateToSettings() {
    add(SettingsDestination)
}

fun EntryProviderScope<NavKey>.settingsScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<SettingsDestination> {
        SettingsRoute(onBack = onBack, snackbarHostState = snackbarHostState)
    }
}
