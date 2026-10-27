package com.example.walklogger.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.feature.calendar.navigation.navigateToCalendar
import com.example.walklogger.feature.dog.navigation.navigateToDog
import com.example.walklogger.feature.log.navigation.navigateToLog
import com.example.walklogger.feature.logwalk.navigation.navigateToActiveWalk
import com.example.walklogger.feature.patterns.navigation.navigateToPatterns

/** Root composable: scaffold + bottom bar + nav host. Each tab draws its own header. */
@Composable
fun WalkLoggerApp(viewModel: WalkLoggerAppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val walkToResume by viewModel.walkToResume.collectAsStateWithLifecycle()
    LaunchedEffect(walkToResume) {
        walkToResume?.let { walkId ->
            navController.navigateToActiveWalk(walkId)
            viewModel.onWalkResumed()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        bottomBar = {
            // Pushed screens (new walk, register dog) are full-screen tasks without the tab bar.
            if (currentDestination?.isTopLevel() != false) {
                WalkLoggerBottomBar(
                    isSelected = { destination ->
                        currentDestination?.hierarchy?.any { it.hasRoute(destination.route) } == true
                    },
                    onSelect = { destination -> navController.navigateToTopLevel(destination) },
                )
            }
        },
    ) { innerPadding ->
        // Only the bottom inset is consumed here; headers handle the status bar themselves
        // so the navy colour runs edge-to-edge behind it. Consuming it lets imePadding below
        // subtract the bottom bar / navigation bar instead of adding the keyboard on top of it.
        val bottomPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        WalkLoggerNavHost(
            navController = navController,
            modifier = Modifier
                .padding(bottomPadding)
                .consumeWindowInsets(bottomPadding),
        )
    }
}

@Composable
private fun WalkLoggerBottomBar(
    isSelected: (TopLevelDestination) -> Boolean,
    onSelect: (TopLevelDestination) -> Unit,
) {
    Column {
        HorizontalDivider(color = AppTheme.colors.outline)
        NavigationBar(containerColor = AppTheme.colors.background, tonalElevation = 0.dp) {
            TopLevelDestination.entries.forEach { destination ->
                NavigationBarItem(
                    selected = isSelected(destination),
                    onClick = { onSelect(destination) },
                    icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                    label = {
                        Text(
                            text = stringResource(destination.label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppTheme.colors.action,
                        selectedTextColor = AppTheme.colors.action,
                        unselectedIconColor = AppTheme.colors.contentMuted,
                        unselectedTextColor = AppTheme.colors.contentMuted,
                        indicatorColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

/**
 * Standard tab-switch behaviour: one back-stack entry per tab, state saved and restored.
 * Screens pushed on top of a tab (e.g. register dog) are dropped first so they are not
 * restored when the user comes back to that tab.
 */
internal fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    popToTopLevel()
    val options = navOptions {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    when (destination) {
        TopLevelDestination.LOG -> navigateToLog(options)
        TopLevelDestination.CALENDAR -> navigateToCalendar(options)
        TopLevelDestination.PATTERNS -> navigateToPatterns(options)
        TopLevelDestination.DOG -> navigateToDog(options)
    }
}

/** Drops every pushed screen, landing on the tab they were opened from. */
internal fun NavHostController.popToTopLevel() {
    while (currentDestination?.isTopLevel() == false) popBackStack()
}

private fun NavDestination.isTopLevel(): Boolean =
    TopLevelDestination.entries.any { hasRoute(it.route) }
