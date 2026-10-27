package com.example.walklogger.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.walklogger.core.ui.theme.AppTheme

@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = MaterialTheme.shapes.extraSmall,
            containerColor = AppTheme.colors.header,
            contentColor = AppTheme.colors.onHeader,
            actionColor = AppTheme.colors.actionLight,
        )
    }
}
