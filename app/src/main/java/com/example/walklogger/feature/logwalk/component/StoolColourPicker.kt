package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private const val SWATCHES_PER_ROW = 5
private val RingWidth = 2.dp
private val RingGap = 3.dp

/**
 * Every [StoolColour] as a round swatch, two rows of five, with the [selected] one ringed and ticked.
 * Tapping the selected swatch again passes null to [onSelect].
 */
@Composable
internal fun StoolColourPicker(
    selected: StoolColour?,
    onSelect: (StoolColour?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        maxItemsInEachRow = SWATCHES_PER_ROW,
    ) {
        StoolColour.entries.forEach { colour ->
            val isSelected = colour == selected
            val label = stringResource(colour.labelRes)
            Box(
                modifier = Modifier
                    .size(AppSize.chipHeight)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                RingWidth,
                                AppTheme.colors.action,
                                CircleShape,
                            )
                        } else {
                            Modifier
                        },
                    )
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
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.onAction,
                    )
                }
            }
        }
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
