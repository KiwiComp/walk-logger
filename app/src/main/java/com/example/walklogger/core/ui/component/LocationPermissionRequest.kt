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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.walklogger.R

/**
 * While [shouldRequest] is true, first explains that only precise location works, then shows the
 * system dialog and reports whether precise location was granted. Coarse is asked for too, as
 * Android 12 and later ignore a request for fine alone; the user may still pick approximate, which
 * the app treats as no location.
 */
@Composable
fun LocationPermissionRequest(shouldRequest: Boolean, onResult: (granted: Boolean) -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            onResult(results[Manifest.permission.ACCESS_FINE_LOCATION] == true)
        }
    // Saved so a rotation while the system dialog is up neither shows the explanation again nor relaunches it.
    var isExplained by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(shouldRequest) {
        if (!shouldRequest) isExplained = false
    }
    if (shouldRequest && !isExplained) {
        AppInfoDialog(
            title = stringResource(R.string.location_precise_title),
            message = stringResource(R.string.location_precise_message),
            onConfirm = {
                isExplained = true
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            },
        )
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
