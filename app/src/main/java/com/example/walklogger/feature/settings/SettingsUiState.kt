package com.example.walklogger.feature.settings

data class SettingsUiState(
    val hasLocationPermission: Boolean = false,
    /** Once asked, Android may no longer show the dialog, so the button can lead to system settings instead. */
    val wasLocationRequested: Boolean = false,
    /** Set when the Route should show the system permission dialog. */
    val shouldRequestLocationPermission: Boolean = false,
)
