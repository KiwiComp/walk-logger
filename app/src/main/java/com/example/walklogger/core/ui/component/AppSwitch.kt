package com.example.walklogger.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** On/off switch in the app palette; on is [AppTheme.colors.action]. */
@Composable
fun AppSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = AppTheme.colors.onAction,
            checkedTrackColor = AppTheme.colors.action,
            checkedBorderColor = AppTheme.colors.action,
            uncheckedThumbColor = AppTheme.colors.card,
            uncheckedTrackColor = AppTheme.colors.outlineStrong,
            uncheckedBorderColor = AppTheme.colors.outlineStrong,
        ),
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
