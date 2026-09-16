package com.example.walklogger.feature.logwalk.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.walklogger.R

/** Asks for a new handler's name; see [AddNameDialog]. */
@Composable
internal fun AddHandlerDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    AddNameDialog(
        title = stringResource(R.string.new_walk_add_handler_title),
        label = stringResource(R.string.new_walk_add_handler_name),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
