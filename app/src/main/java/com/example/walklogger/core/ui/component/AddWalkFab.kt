package com.example.walklogger.core.ui.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Floating "+ Add walk" action pinned over a tab's content. */
@Composable
fun AddWalkFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        text = {
            Text(stringResource(R.string.add_walk), style = MaterialTheme.typography.titleMedium)
        },
        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
        onClick = onClick,
        shape = CircleShape,
        containerColor = AppTheme.colors.action,
        contentColor = AppTheme.colors.onAction,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun AddWalkFabPreview() {
    WalkLoggerTheme { AddWalkFab(onClick = {}) }
}
