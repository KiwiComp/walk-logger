package com.example.walklogger.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Tells the user something they must acknowledge; OK is the only way to close it. */
@Composable
fun AppInfoDialog(title: String, message: String, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        shape = MaterialTheme.shapes.extraSmall,
        containerColor = AppTheme.colors.background,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
    )
}

@Preview
@Composable
private fun AppInfoDialogPreview() {
    WalkLoggerTheme {
        AppInfoDialog(
            title = stringResource(R.string.location_precise_title),
            message = stringResource(R.string.location_precise_message),
            onConfirm = {},
        )
    }
}
