package com.example.walklogger.core.ui.message

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource

/**
 * Shows a UiState's [message] in the app's snackbar, then calls [onConsume] so the ViewModel clears it.
 * If the screen leaves before that, the message stays in state and shows when it returns.
 */
@Composable
fun UserMessageEffect(
    @StringRes message: Int?,
    snackbarHostState: SnackbarHostState,
    onConsume: (Int) -> Unit,
) {
    if (message == null) return
    val text = stringResource(message)
    val currentOnConsume by rememberUpdatedState(onConsume)
    LaunchedEffect(message, snackbarHostState) {
        snackbarHostState.showSnackbar(text)
        currentOnConsume(message)
    }
}
