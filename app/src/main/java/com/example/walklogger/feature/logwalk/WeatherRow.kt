package com.example.walklogger.feature.logwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.component.SegmentedToggleSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val LabelWidth = 48.dp

/** A weather condition's [label] beside a [SegmentedToggle] of its values. */
@Composable
internal fun <T> WeatherRow(
    label: String,
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            modifier = Modifier.width(LabelWidth),
        )
        SegmentedToggle(
            options = options,
            selected = selected,
            onSelect = onSelect,
            label = optionLabel,
            modifier = Modifier.weight(1f),
            size = SegmentedToggleSize.Compact,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherRowPreview() {
    WalkLoggerTheme {
        WeatherRow(
            label = "Rain",
            options = Rain.entries,
            selected = Rain.LIGHT,
            onSelect = {},
            optionLabel = { it.name.lowercase().replaceFirstChar(Char::titlecase) },
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
