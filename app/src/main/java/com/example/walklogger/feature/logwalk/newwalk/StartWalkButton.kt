package com.example.walklogger.feature.logwalk.newwalk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val PlayCircleSize = 56.dp

/** The big primary action: starts the timer. [subtitle] says what tapping does, or why it is disabled. */
@Composable
internal fun StartWalkButton(
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val foreground = if (enabled) colors.onAction else colors.onDisabled
    val shape = RoundedCornerShape(AppRadius.xl)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (enabled) colors.action else colors.disabled)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(AppSpacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Box(
            modifier = Modifier
                .size(PlayCircleSize)
                .background(if (enabled) colors.card else colors.onDisabled, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = if (enabled) colors.action else colors.disabled,
            )
        }
        Column {
            Text(
                text = stringResource(R.string.new_walk_start),
                style = MaterialTheme.typography.headlineMedium,
                color = foreground,
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = foreground)
        }
    }
}

@Preview
@Composable
private fun StartWalkButtonPreview() {
    WalkLoggerTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            StartWalkButton(subtitle = "Timer starts when you tap", enabled = true, onClick = {})
            StartWalkButton(subtitle = "Choose who is walking first.", enabled = false, onClick = {
            })
        }
    }
}
