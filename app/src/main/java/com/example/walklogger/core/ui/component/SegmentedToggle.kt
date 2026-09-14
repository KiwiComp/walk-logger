package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.CardSurface
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val DividerWidth = 1.dp

/**
 * Joined row of equal-width segments where exactly one can be selected
 * (equipment, rain, temperature, wind). [selected] is null until the user picks.
 */
@Composable
fun <T> SegmentedToggle(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.extraSmall
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSize.buttonHeight)
            .clip(shape)
            .border(1.dp, Outline, shape)
            .selectableGroup(),
    ) {
        options.forEachIndexed { index, option ->
            if (index > 0) {
                Box(Modifier.width(DividerWidth).fillMaxHeight().background(Outline))
            }
            Segment(
                text = label(option),
                isSelected = option == selected,
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
private fun RowScope.Segment(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(if (isSelected) Steel else CardSurface)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = AppSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) OnNavy else Ink,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedTogglePreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            SegmentedToggle(
                options = listOf("Harness", "Collar"),
                selected = "Harness",
                onSelect = {},
                label = { it },
            )
            SegmentedToggle(
                options = listOf("Cold", "Cool", "Neutral", "Warm", "Hot"),
                selected = null,
                onSelect = {},
                label = { it },
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}
