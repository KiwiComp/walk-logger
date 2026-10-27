package com.example.walklogger.feature.logwalk.activewalk

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

@Composable
internal fun DiscardWalkDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraSmall,
        containerColor = AppTheme.colors.background,
        title = { Text(stringResource(R.string.active_walk_discard_confirm_title)) },
        text = { Text(stringResource(R.string.active_walk_discard_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.active_walk_discard_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@Preview
@Composable
private fun DiscardWalkDialogPreview() {
    WalkLoggerTheme {
        DiscardWalkDialog(onConfirm = {}, onDismiss = {})
    }
}
