package com.example.walklogger.feature.logwalk.component

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Shows the system dialog for location while [shouldRequest] is true and reports whether any location
 * was granted. Precise location is asked for now, as route tracking will need it; from Android 12 the
 * user may still pick approximate, which is enough for the weather.
 */
@Composable
internal fun LocationPermissionRequest(shouldRequest: Boolean, onResult: (granted: Boolean) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        onResult(results.values.any { it })
    }
    LaunchedEffect(shouldRequest) {
        if (shouldRequest) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}
