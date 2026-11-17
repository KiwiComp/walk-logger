package com.example.walklogger.core.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * Floating "+ Add walk" action pinned over a tab's content. It slides up by [snackbarHeight] so a
 * snackbar shown by the app's Scaffold never covers it.
 */
@Composable
fun AddWalkFab(onClick: () -> Unit, modifier: Modifier = Modifier, snackbarHeight: Dp = 0.dp) {
    val lift by animateDpAsState(snackbarHeight, label = "fabLift")
    ExtendedFloatingActionButton(
        text = {
            Text(stringResource(R.string.add_walk), style = MaterialTheme.typography.titleMedium)
        },
        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
        onClick = onClick,
        shape = CircleShape,
        containerColor = AppTheme.colors.action,
        contentColor = AppTheme.colors.onAction,
        modifier = modifier.offset { IntOffset(0, -lift.roundToPx()) },
    )
}

@Preview
@Composable
private fun AddWalkFabPreview() {
    WalkLoggerTheme { AddWalkFab(onClick = {}) }
}
