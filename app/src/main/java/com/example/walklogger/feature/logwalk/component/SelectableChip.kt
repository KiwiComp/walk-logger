package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData

/** Label size of a [SelectableChip]; the chips themselves keep the same height. */
internal enum class SelectableChipSize {
    /** Bold title text for primary choices such as handlers. */
    Regular,

    /** Body-small text for dense rows such as the behavioural issues. */
    Compact,
}

/**
 * [items], any number of which can be selected, laid out as wrapping chips and ending with a dashed
 * [addLabel] chip. Selection is decided per item by [isSelected], so callers can key it on a name or an id.
 */
@Composable
internal fun <T> SelectableChip(
    items: List<T>,
    isSelected: (T) -> Boolean,
    label: @Composable (T) -> String,
    onToggle: (T) -> Unit,
    addLabel: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    size: SelectableChipSize = SelectableChipSize.Regular,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        items.forEach { item ->
            Chip(text = label(item), isSelected = isSelected(item), onToggle = { onToggle(item) }, size = size)
        }
        AddChip(text = addLabel, onClick = onAdd, chipSize = size)
    }
}

@Composable
private fun Chip(text: String, isSelected: Boolean, onToggle: () -> Unit, size: SelectableChipSize) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .height(AppSize.chipHeight)
            .clip(shape)
            .background(if (isSelected) Steel else CardSurface)
            .border(1.dp, if (isSelected) Steel else Outline, shape)
            .toggleable(value = isSelected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = AppSpacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = size.textStyle(),
            color = if (isSelected) OnNavy else Ink,
        )
    }
}

@Composable
private fun AddChip(text: String, onClick: () -> Unit, chipSize: SelectableChipSize) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .height(AppSize.chipHeight)
            .clip(shape)
            .drawBehind {
                drawOutline(
                    color = Outline,
                    outline = shape.createOutline(size, layoutDirection, this),
                    style = Stroke(width = 3f, pathEffect = dash),
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppSpacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = chipSize.textStyle(),
            color = Ink,
        )
    }
}

@Composable
private fun SelectableChipSize.textStyle() = when (this) {
    SelectableChipSize.Regular -> MaterialTheme.typography.titleSmall
    SelectableChipSize.Compact -> MaterialTheme.typography.bodySmall
}

@Preview(showBackground = true)
@Composable
private fun SelectableChipPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            SelectableChip(
                items = LogWalkPreviewData.handlers,
                isSelected = { it in setOf("Anna", "Jonas") },
                label = { it },
                onToggle = {},
                addLabel = "+ Someone else",
                onAdd = {},
            )
            SelectableChip(
                items = LogWalkPreviewData.behaviourIssues,
                isSelected = { it.id in setOf(1L, 4L) },
                label = { it.label() },
                onToggle = {},
                addLabel = "+ Other issue",
                onAdd = {},
                modifier = Modifier.padding(top = AppSpacing.lg),
                size = SelectableChipSize.Compact,
            )
        }
    }
}
