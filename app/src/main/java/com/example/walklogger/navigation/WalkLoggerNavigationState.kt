package com.example.walklogger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * One back stack per tab, so a tab keeps its state while another is shown.
 * Log is the start tab: back from another tab's root lands there.
 */
class WalkLoggerNavigationState(
    currentTab: MutableState<TopLevelDestination>,
    val backStacks: Map<TopLevelDestination, MutableList<NavKey>>,
) {
    var currentTab by currentTab
        private set

    val currentStack: MutableList<NavKey> get() = backStacks.getValue(currentTab)

    val isOnTopLevel: Boolean get() = currentStack.size == 1

    /** Pushed screens (e.g. register dog) are dropped first so they are not restored on return. */
    fun selectTab(tab: TopLevelDestination) {
        popToTopLevel()
        currentTab = tab
        popToTopLevel()
    }

    /** Drops every pushed screen, landing on the tab they were opened from. */
    fun popToTopLevel() {
        while (currentStack.size > 1) currentStack.removeAt(currentStack.lastIndex)
    }

    fun goBack() {
        when {
            !isOnTopLevel -> currentStack.removeAt(currentStack.lastIndex)
            currentTab != TopLevelDestination.LOG -> currentTab = TopLevelDestination.LOG
        }
    }

    internal val tabsShown: List<TopLevelDestination> get() = listOf(
        TopLevelDestination.LOG,
        currentTab,
    ).distinct()
}

@Composable
fun rememberWalkLoggerNavigationState(): WalkLoggerNavigationState {
    val currentTab = rememberSaveable { mutableStateOf(TopLevelDestination.LOG) }
    val backStacks = TopLevelDestination.entries.associateWith { rememberNavBackStack(it.key) }
    return remember(currentTab, backStacks) {
        WalkLoggerNavigationState(currentTab, backStacks)
    }
}

/**
 * Every tab's stack is decorated, not only the visible ones, so a hidden tab keeps its
 * saved state and ViewModels. The Log stack sits under the current tab so back can reach it.
 */
@Composable
fun WalkLoggerNavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): List<NavEntry<NavKey>> {
    val decorated = backStacks.mapValues { (_, stack) ->
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider,
        )
    }
    return tabsShown.flatMap { decorated.getValue(it) }
}
