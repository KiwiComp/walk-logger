package com.example.walklogger.feature.settings

import androidx.annotation.StringRes
import com.example.walklogger.core.model.ThemeMode

data class SettingsUiState(
    val hasLocationPermission: Boolean = false,
    /** Once asked, Android may no longer show the dialog, so the button can lead to system settings instead. */
    val wasLocationRequested: Boolean = false,
    /** Set when the Route should show the system permission dialog. */
    val shouldRequestLocationPermission: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    @param:StringRes val userMessage: Int? = null,
)
