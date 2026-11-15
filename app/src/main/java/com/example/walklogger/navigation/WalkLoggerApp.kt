package com.example.walklogger.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.core.ui.component.AppSnackbarHost
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.feature.logwalk.navigation.navigateToActiveWalk
import kotlinx.coroutines.launch

/** Root composable: scaffold + bottom bar + nav host. Each tab draws its own header. */
@Composable
fun WalkLoggerApp(
    modifier: Modifier = Modifier,
    viewModel: WalkLoggerAppViewModel = hiltViewModel(),
) {
    val navigationState = rememberWalkLoggerNavigationState()

    val walkToResume by viewModel.walkToResume.collectAsStateWithLifecycle()
    LaunchedEffect(walkToResume) {
        walkToResume?.let { walkId ->
            navigationState.currentStack.navigateToActiveWalk(walkId)
            viewModel.onWalkResumed()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val messageScope = rememberCoroutineScope()
    val showMessage: (Int) -> Unit = remember(messageScope, snackbarHostState, resources) {
        { message ->
            messageScope.launch { snackbarHostState.showSnackbar(resources.getString(message)) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Pushed screens (new walk, register dog) are full-screen tasks without the tab bar.
            if (navigationState.isOnTopLevel) {
                WalkLoggerBottomBar(
                    isSelected = { it == navigationState.currentTab },
                    onSelect = navigationState::selectTab,
                )
            }
        },
    ) { innerPadding ->
        // Only the bottom inset is consumed here; headers handle the status bar themselves
        // so the navy colour runs edge-to-edge behind it. Consuming it lets imePadding below
        // subtract the bottom bar / navigation bar instead of adding the keyboard on top of it.
        val bottomPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        WalkLoggerNavHost(
            navigationState = navigationState,
            snackbarHostState = snackbarHostState,
            onShowMessage = showMessage,
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
