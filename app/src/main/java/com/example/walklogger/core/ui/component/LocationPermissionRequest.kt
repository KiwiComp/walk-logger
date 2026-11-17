package com.example.walklogger.core.ui.component

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Shows the system dialog for location while [shouldRequest] is true and reports whether any location
 * was granted. Precise location is asked for now, as route tracking will need it; from Android 12 the
 * user may still pick approximate, which is enough for the weather.
 */
@Composable
fun LocationPermissionRequest(shouldRequest: Boolean, onResult: (granted: Boolean) -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            onResult(results.values.any { it })
        }
    LaunchedEffect(shouldRequest) {
        if (shouldRequest) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }
}

/**
 * Returns what an "allow location" button does: [onRequest] while Android will still show the
 * dialog, otherwise the app's system settings. After a second denial Android stops showing it
 * and the rationale turns false, which is the only signal of that.
 */
@Composable
fun rememberAllowLocation(wasRequested: Boolean, onRequest: () -> Unit): () -> Unit {
    val activity = LocalActivity.current
    val currentWasRequested by rememberUpdatedState(wasRequested)
    val currentOnRequest by rememberUpdatedState(onRequest)
    return remember(activity) {
        {
            val canShowDialog = !currentWasRequested ||
                activity?.shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == true
            if (canShowDialog || activity == null) {
                currentOnRequest()
            } else {
                activity.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", activity.packageName, null),
                    ),
                )
            }
        }
    }
}
