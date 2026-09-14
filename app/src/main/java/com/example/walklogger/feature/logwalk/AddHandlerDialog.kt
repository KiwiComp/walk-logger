package com.example.walklogger.feature.logwalk

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.theme.Canvas
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Asks for a new handler's name. [onConfirm] receives the untrimmed input; blank input can't be confirmed. */
@Composable
internal fun AddHandlerDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraSmall,
        containerColor = Canvas,
        title = { Text(stringResource(R.string.new_walk_add_handler_title)) },
        text = {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.new_walk_add_handler_name),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.dialog_ok))
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
private fun AddHandlerDialogPreview() {
    WalkLoggerTheme {
        AddHandlerDialog(onConfirm = {}, onDismiss = {})
    }
}
