package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * Bordered small-caps text button for the header ("Today"); the text sibling of [HeaderIconButton].
 * When not [visible] it is drawn transparent and inert but still takes up its space, so a row it sits in
 * keeps its height whether or not the button is needed.
 */
@Composable
fun HeaderTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    val shape = MaterialTheme.shapes.extraSmall
    SectionLabel(
        text = text,
        color = AppTheme.colors.onHeader,
        modifier = modifier
            .alpha(if (visible) 1f else 0f)
            .clip(shape)
            .border(1.dp, AppTheme.colors.outlineOnHeader, shape)
            .clickable(enabled = visible, role = Role.Button, onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    )
}

@Preview
@Composable
private fun HeaderTextButtonPreview() {
    WalkLoggerTheme {
        HeaderTextButton(text = "Today", onClick = {
        }, modifier = Modifier.background(AppTheme.colors.header))
    }
}
