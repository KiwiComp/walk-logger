package com.example.walklogger.feature.patterns.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.feature.patterns.PatternsRoute
import kotlinx.serialization.Serializable

@Serializable
data object PatternsDestination : NavKey

fun EntryProviderScope<NavKey>.patternsScreen() {
    entry<PatternsDestination> { PatternsRoute() }
}
