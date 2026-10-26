package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.ChipSurface
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.Navy
import com.example.walklogger.core.ui.theme.NavyLight
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.OnNavyMuted
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.OutlineOnNavy
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

enum class AppSwitchStyle {
    /** On a light surface. */
    Default,

    /** On the header surface; the stool switch during a walk. */
    Inverse,
}

/** On/off switch in the app palette; on is [Steel] in both styles. */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    style: AppSwitchStyle = AppSwitchStyle.Default,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = style.colors(),
    )
}

@Composable
private fun AppSwitchStyle.colors(): SwitchColors = when (this) {
    AppSwitchStyle.Default -> SwitchDefaults.colors(
        checkedThumbColor = OnNavy,
        checkedTrackColor = Steel,
        checkedBorderColor = Steel,
        uncheckedThumbColor = InkMuted,
        uncheckedTrackColor = ChipSurface,
        uncheckedBorderColor = Outline,
    )
    AppSwitchStyle.Inverse -> SwitchDefaults.colors(
        checkedThumbColor = OnNavy,
        checkedTrackColor = Steel,
        checkedBorderColor = Steel,
        uncheckedThumbColor = OnNavyMuted,
        uncheckedTrackColor = NavyLight,
        uncheckedBorderColor = OutlineOnNavy,
    )
}

@Preview(showBackground = true)
@Composable
private fun AppSwitchPreview() {
    WalkLoggerTheme {
        Row(modifier = Modifier.padding(AppSpacing.lg)) {
            AppSwitch(checked = false, onCheckedChange = {})
            AppSwitch(checked = true, onCheckedChange = {})
        }
    }
}

@Preview
@Composable
private fun AppSwitchInversePreview() {
    WalkLoggerTheme {
        Row(modifier = Modifier.background(Navy).padding(AppSpacing.lg)) {
            AppSwitch(checked = false, onCheckedChange = {}, style = AppSwitchStyle.Inverse)
            AppSwitch(checked = true, onCheckedChange = {}, style = AppSwitchStyle.Inverse)
        }
    }
}
