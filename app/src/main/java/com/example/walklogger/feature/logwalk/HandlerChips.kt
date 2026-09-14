package com.example.walklogger.feature.logwalk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.CardSurface
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val ChipHeight = 44.dp

/** Handler names, any number of which can be selected, ending with a dashed "+ Someone else" chip. */
@Composable
internal fun HandlerChips(
    handlers: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        handlers.forEach { handler ->
            HandlerChip(name = handler, isSelected = handler in selected, onToggle = { onToggle(handler) })
        }
        AddHandlerChip(onClick = onAdd)
    }
}

@Composable
private fun HandlerChip(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .height(ChipHeight)
            .clip(shape)
            .background(if (isSelected) Steel else CardSurface)
            .border(1.dp, if (isSelected) Steel else Outline, shape)
            .toggleable(value = isSelected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = AppSpacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) OnNavy else Ink,
        )
    }
}

@Composable
private fun AddHandlerChip(onClick: () -> Unit) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .height(ChipHeight)
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
            text = stringResource(R.string.new_walk_someone_else),
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HandlerChipsPreview() {
    WalkLoggerTheme {
        HandlerChips(
            handlers = LogWalkPreviewData.handlers,
            selected = setOf("Anna", "Jonas"),
            onToggle = {},
            onAdd = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
