package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private const val SwatchesPerRow = 5
private val RingWidth = 2.dp
private val RingGap = 3.dp

/**
 * Every [StoolColour] as a round swatch, two rows of five, with the [selected] one ringed and named below;
 * the name's line stays reserved while nothing is selected so picking a colour does not shift the form.
 * Tapping the selected swatch again passes null to [onSelect].
 */
@Composable
internal fun StoolColourPicker(
    selected: StoolColour?,
    onSelect: (StoolColour?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            maxItemsInEachRow = SwatchesPerRow,
        ) {
            StoolColour.entries.forEach { colour ->
                val isSelected = colour == selected
                val label = stringResource(colour.labelRes)
                Box(
                    modifier = Modifier
                        .size(AppSize.chipHeight)
                        .clip(CircleShape)
                        .then(if (isSelected) Modifier.border(RingWidth, Steel, CircleShape) else Modifier)
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onSelect(if (isSelected) null else colour) },
                        )
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    StoolSwatch(
                        colour = colour,
                        size = AppSize.chipHeight - (RingWidth + RingGap) * 2,
                    )
                }
            }
        }
        Text(
            text = selected?.let { stringResource(it.labelRes) }.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolColourPickerPreview() {
    WalkLoggerTheme {
        StoolColourPicker(
            selected = StoolColour.WHITE_SPECKED,
            onSelect = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
