package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val ButtonSize = 44.dp

/** Bordered square icon button for the header's navigation and action slots (back, close, delete). */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = MaterialTheme.shapes.extraSmall
    IconButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = IconButtonDefaults.iconButtonColors(contentColor = AppTheme.colors.onHeader),
        modifier = modifier
            .size(ButtonSize)
            .border(1.dp, AppTheme.colors.outlineOnHeader, shape),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Preview
@Composable
private fun HeaderIconButtonPreview() {
    WalkLoggerTheme {
        HeaderIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Back",
            onClick = {},
            modifier = Modifier.background(AppTheme.colors.header),
        )
    }
}
