package com.example.walklogger.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.component.StatusTag
import com.example.walklogger.core.ui.component.StatusTagTone
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** "Location" section: whether the app may use the location, why, and Allow location while it may not. */
@Composable
internal fun LocationSetting(
    hasLocationPermission: Boolean,
    onAllowLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        SectionLabel(
            text = stringResource(R.string.settings_location),
            color = AppTheme.colors.contentMuted,
            style = MaterialTheme.typography.labelLarge,
        )
        val shape = RoundedCornerShape(AppRadius.xl)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(AppTheme.colors.card)
                .border(1.dp, AppTheme.colors.outline, shape)
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = AppTheme.colors.content,
                )
                Text(
                    text = stringResource(R.string.settings_location),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.content,
                    modifier = Modifier.weight(1f),
                )
                if (hasLocationPermission) {
                    StatusTag(
                        text = stringResource(R.string.settings_location_allowed),
                        tone = StatusTagTone.Positive,
                    )
                } else {
                    StatusTag(
                        text = stringResource(R.string.settings_location_not_allowed),
                        tone = StatusTagTone.Warning,
                    )
                }
            }
            Text(
                text = stringResource(R.string.settings_location_description),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.contentSecondary,
            )
            if (!hasLocationPermission) {
                AppButton(
                    text = stringResource(R.string.settings_location_allow),
                    onClick = onAllowLocation,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationSettingAllowedPreview() {
    WalkLoggerTheme {
        LocationSetting(
            hasLocationPermission = true,
            onAllowLocation = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationSettingNotAllowedPreview() {
    WalkLoggerTheme {
        LocationSetting(
            hasLocationPermission = false,
            onAllowLocation = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
