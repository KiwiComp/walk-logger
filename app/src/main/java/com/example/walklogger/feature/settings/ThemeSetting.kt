package com.example.walklogger.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.ThemeMode
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** "Appearance" section: light, dark or the device's theme. */
@Composable
internal fun ThemeSetting(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        SectionLabel(
            text = stringResource(R.string.settings_theme),
            color = AppTheme.colors.contentMuted,
            style = MaterialTheme.typography.labelLarge,
        )
        val shape = RoundedCornerShape(AppRadius.xl)
        SegmentedToggle(
            options = listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System),
            selected = themeMode,
            onSelect = onThemeModeChange,
            label = { stringResource(it.labelRes()) },
            modifier = Modifier
                .clip(shape)
                .background(AppTheme.colors.card)
                .border(1.dp, AppTheme.colors.outline, shape)
                .padding(AppSpacing.lg),
        )
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.Light -> R.string.settings_theme_light
    ThemeMode.Dark -> R.string.settings_theme_dark
    ThemeMode.System -> R.string.settings_theme_system
}

@Preview(showBackground = true)
@Composable
private fun ThemeSettingLightPreview() {
    WalkLoggerTheme(darkTheme = false) {
        ThemeSetting(
            themeMode = ThemeMode.Light,
            onThemeModeChange = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview
@Composable
private fun ThemeSettingDarkPreview() {
    WalkLoggerTheme(darkTheme = true) {
        ThemeSetting(
            themeMode = ThemeMode.Dark,
            onThemeModeChange = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
