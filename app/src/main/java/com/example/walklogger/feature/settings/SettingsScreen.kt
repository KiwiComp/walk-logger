package com.example.walklogger.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.model.ThemeMode
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.LocationPermissionRequest
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.component.rememberAllowLocation
import com.example.walklogger.core.ui.message.UserMessageEffect
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UserMessageEffect(
        message = uiState.userMessage,
        snackbarHostState = snackbarHostState,
        onConsume = viewModel::userMessageShown,
    )
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshLocationPermission()
        onPauseOrDispose {}
    }
    LocationPermissionRequest(
        shouldRequest = uiState.shouldRequestLocationPermission,
        onResult = viewModel::onLocationPermissionResult,
    )
    val allowLocation = rememberAllowLocation(
        wasRequested = uiState.wasLocationRequested,
        onRequest = viewModel::requestLocationPermission,
    )
    SettingsScreen(
        uiState = uiState,
        onAllowLocation = allowLocation,
        onThemeModeChange = viewModel::setThemeMode,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onAllowLocation: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.settings_title),
            navigationIcon = {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.back),
                    onClick = onBack,
                )
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg)
                .padding(top = AppSpacing.md, bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
        ) {
            ThemeSetting(
                themeMode = uiState.themeMode,
                onThemeModeChange = onThemeModeChange,
            )
            LocationSetting(
                hasLocationPermission = uiState.hasLocationPermission,
                onAllowLocation = onAllowLocation,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    WalkLoggerTheme {
        SettingsScreen(
            uiState = SettingsUiState(),
            onAllowLocation = {},
            onThemeModeChange = {},
            onBack = {},
        )
    }
}
