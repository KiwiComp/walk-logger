package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Every [WeatherCondition] as a square icon chip; [selected] is filled like a selected [SelectableChip]. */
@Composable
internal fun WeatherConditionPicker(
    selected: WeatherCondition?,
    isDay: Boolean,
    onSelect: (WeatherCondition) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        WeatherCondition.entries.forEach { condition ->
            val isSelected = condition == selected
            val shape = MaterialTheme.shapes.extraSmall
            Box(
                modifier = Modifier
                    .size(AppSize.chipHeight)
                    .clip(shape)
                    .background(if (isSelected) AppTheme.colors.action else AppTheme.colors.card)
                    .border(1.dp, if (isSelected) AppTheme.colors.action else AppTheme.colors.outline, shape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(condition) }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(condition.iconRes(isDay)),
                    contentDescription = stringResource(condition.labelRes),
                    tint = if (isSelected) AppTheme.colors.onHeader else AppTheme.colors.content,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherConditionPickerPreview() {
    WalkLoggerTheme {
        WeatherConditionPicker(
            selected = WeatherCondition.RAIN,
            isDay = true,
            onSelect = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
