package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val CheckSize = 18.dp

enum class StatusTagTone {
    /** Green with a tick: something is on or chosen ("Active", "Allowed"). */
    Positive,

    /** Amber: something still needs the user's attention ("Not allowed"). */
    Warning,
}

/** Small rounded label stating a status at the end of a row or card header. */
@Composable
fun StatusTag(text: String, tone: StatusTagTone, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val (background, content) = when (tone) {
        StatusTagTone.Positive -> colors.actionLight to colors.onActionLight
        StatusTagTone.Warning -> colors.warningLight to colors.onWarningLight
    }
    Row(
        modifier = modifier
            .background(background, CircleShape)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        if (tone == StatusTagTone.Positive) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(CheckSize),
            )
        }
        Text(text = text, style = MaterialTheme.typography.titleSmall, color = content)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusTagPreview() {
    WalkLoggerTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            StatusTag(text = "Allowed", tone = StatusTagTone.Positive)
            StatusTag(text = "Not allowed", tone = StatusTagTone.Warning)
        }
    }
}
