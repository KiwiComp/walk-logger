package com.example.walklogger.feature.logwalk.newwalk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val ButtonHeight = 180.dp

/** The tall primary action: starts the timer. [summary] is "handlers · equipment", or the reason it is disabled. */
@Composable
internal fun StartWalkButton(
    summary: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val foreground = if (enabled) AppTheme.colors.onHeader else AppTheme.colors.contentMuted
    val mutedForeground =
        if (enabled) AppTheme.colors.onHeaderMuted else AppTheme.colors.contentMuted
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(if (enabled) AppTheme.colors.action else AppTheme.colors.chip)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = AppSpacing.xxxl, vertical = AppSpacing.xxxl),
    ) {
        SectionLabel(
            text = stringResource(R.string.new_walk_timer_starts_now),
            color = mutedForeground,
        )
        Spacer(Modifier.height(AppSpacing.md))
        Text(
            text = stringResource(R.string.new_walk_start),
            style = MaterialTheme.typography.headlineLarge,
            color = foreground,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = mutedForeground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StartWalkButtonPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            StartWalkButton(summary = "Anna · harness", enabled = true, onClick = {})
            StartWalkButton(
                summary = "Choose who is walking first.",
                enabled = false,
                onClick = {},
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}
