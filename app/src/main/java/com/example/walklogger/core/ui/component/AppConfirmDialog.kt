package com.example.walklogger.core.ui.component

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

/** Asks before something that cannot be undone, such as discarding a walk; Cancel only closes it. */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraSmall,
        containerColor = AppTheme.colors.background,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
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
private fun AppConfirmDialogPreview() {
    WalkLoggerTheme {
        AppConfirmDialog(
            title = stringResource(R.string.walk_detail_discard_confirm_title),
            message = stringResource(R.string.walk_detail_discard_confirm_message),
            confirmLabel = stringResource(R.string.walk_detail_discard_confirm),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
