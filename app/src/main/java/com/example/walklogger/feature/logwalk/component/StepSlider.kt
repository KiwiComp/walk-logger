package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import kotlin.math.roundToInt

private val ThumbSize = 20.dp
private val ThumbBorderWidth = 2.dp
private val TrackHeight = 4.dp

/**
 * Picks one of [options] on a slider with each option's [label] under its stop. While [selected] is null
 * only the track shows; a tap or drag anywhere on it picks the option there.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> StepSlider(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    style: FormStyle = FormStyle.Default,
) {
    val colors = AppTheme.colors
    val railColor = if (style == FormStyle.Default) colors.rail else colors.headerRaised
    val ringColor = if (style == FormStyle.Default) colors.card else colors.header
    val selectedIndex = options.indexOf(selected).takeIf { it >= 0 }
    val restingIndex = options.lastIndex / 2
    // The slider only reports a change, so a tap on the hidden resting stop would otherwise do nothing.
    var changedDuringGesture by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Slider(
            value = (selectedIndex ?: restingIndex).toFloat(),
            onValueChange = {
                changedDuringGesture = true
                onSelect(options[it.roundToInt()])
            },
            onValueChangeFinished = {
                if (!changedDuringGesture && selectedIndex == null) onSelect(options[restingIndex])
                changedDuringGesture = false
            },
            valueRange = 0f..options.lastIndex.toFloat(),
            steps = options.size - 2,
            thumb = {
                Box(
                    Modifier
                        .size(ThumbSize)
                        .then(
                            if (selectedIndex != null) {
                                Modifier
                                    .background(colors.action, CircleShape)
                                    .border(ThumbBorderWidth, ringColor, CircleShape)
                            } else {
                                Modifier
                            },
                        ),
                )
            },
            track = { state ->
                val start = state.valueRange.start
                val fraction = (state.value - start) / (state.valueRange.endInclusive - start)
                val fillColor = if (selectedIndex != null) colors.action else railColor
                Canvas(Modifier.fillMaxWidth().height(TrackHeight)) {
                    val y = size.height / 2
                    val stroke = TrackHeight.toPx()
                    drawLine(
                        railColor,
                        Offset(0f, y),
                        Offset(size.width, y),
                        stroke,
                        StrokeCap.Round,
                    )
                    drawLine(
                        fillColor,
                        Offset(0f, y),
                        Offset(size.width * fraction, y),
                        stroke,
                        StrokeCap.Round,
                    )
                }
            },
            modifier = Modifier.heightIn(max = AppSize.chipHeight),
        )
        StopLabels(labels = options.map { label(it) }, color = style.muted)
    }
}

/** Each label centred under its stop, nudged inwards at the ends so it stays inside the slider's width. */
@Composable
private fun StopLabels(labels: List<String>, color: Color) {
    Layout(
        content = {
            labels.forEach {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = color)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val width = constraints.maxWidth
        val inset = ThumbSize.toPx() / 2
        val span = width - 2 * inset
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val fraction = if (placeables.size >
                    1
                ) {
                    index / placeables.lastIndex.toFloat()
                } else {
                    0f
                }
                val x = (inset + span * fraction - placeable.width / 2f).roundToInt()
                placeable.placeRelative(
                    x.coerceIn(0, (width - placeable.width).coerceAtLeast(0)),
                    0,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StepSliderPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            StepSlider(
                options = StoolTexture.entries,
                selected = StoolTexture.FIRM,
                onSelect = {},
                label = { stringResource(it.labelRes) },
            )
            StepSlider(
                options = StoolTexture.entries,
                selected = null,
                onSelect = {},
                label = { stringResource(it.labelRes) },
            )
        }
    }
}
