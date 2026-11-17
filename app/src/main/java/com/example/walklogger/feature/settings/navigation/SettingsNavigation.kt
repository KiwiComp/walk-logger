package com.example.walklogger.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsDestination : NavKey

fun MutableList<NavKey>.navigateToSettings() {
    add(SettingsDestination)
}

fun EntryProviderScope<NavKey>.settingsScreen(onBack: () -> Unit) {
    entry<SettingsDestination> {
        SettingsRoute(onBack = onBack)
    }
}
