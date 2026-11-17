package com.example.walklogger.feature.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppButtonStyle
import com.example.walklogger.core.ui.component.AppButtonWidth
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

@Composable
internal fun LocationSetting(
    hasLocationPermission: Boolean,
    onAllowLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(
            text = stringResource(R.string.settings_location),
            color = AppTheme.colors.contentMuted,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppTheme.colors.outline, MaterialTheme.shapes.extraSmall)
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(
                    if (hasLocationPermission) {
                        R.string.settings_location_allowed
                    } else {
                        R.string.settings_location_not_allowed
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.content,
            )
            Text(
                text = stringResource(R.string.settings_location_description),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.contentMuted,
            )
            if (!hasLocationPermission) {
                AppButton(
                    text = stringResource(R.string.settings_location_allow),
                    onClick = onAllowLocation,
                    style = AppButtonStyle.Secondary,
                    width = AppButtonWidth.Wrap,
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
