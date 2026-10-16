package com.example.walklogger.feature.dog.registerdog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.Canvas
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

@Composable
internal fun DeleteDogDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraSmall,
        containerColor = Canvas,
        title = { Text(stringResource(R.string.edit_dog_delete_confirm_title)) },
        text = { Text(stringResource(R.string.edit_dog_delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.edit_dog_delete_confirm))
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
private fun DeleteDogDialogPreview() {
    WalkLoggerTheme {
        DeleteDogDialog(onConfirm = {}, onDismiss = {})
    }
}
