package com.example.walklogger.feature.logwalk.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.component.SegmentedToggleSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * "Stool" form section. It is never folded away: [stool] stays null until the handler actively picks
 * Yes or No, and Clear returns it to null. The description rows only show after Yes.
 */
@Composable
internal fun StoolSection(
    stool: Stool?,
    onPassedSelected: (Boolean) -> Unit,
    onQuantitySelected: (StoolQuantity) -> Unit,
    onTextureSelected: (StoolTexture) -> Unit,
    onColourSelected: (StoolColour?) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormSection(
        label = stringResource(R.string.log_past_walk_stool),
        actionLabel = if (stool != null) stringResource(R.string.form_section_clear) else null,
        onAction = onClear,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            SegmentedToggle(
                options = listOf(true, false),
                selected = stool?.passed,
                onSelect = onPassedSelected,
                label = { stringResource(if (it) R.string.stool_passed_yes else R.string.stool_passed_no) },
            )
            AnimatedVisibility(visible = stool?.passed == true) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    DescriptorRow(label = stringResource(R.string.stool_quantity)) {
                        SegmentedToggle(
                            options = StoolQuantity.entries,
                            selected = stool?.quantity,
                            onSelect = onQuantitySelected,
                            label = { stringResource(it.labelRes) },
                            size = SegmentedToggleSize.Compact,
                        )
                    }
                    DescriptorRow(label = stringResource(R.string.stool_texture)) {
                        SegmentedToggle(
                            options = StoolTexture.entries,
                            selected = stool?.texture,
                            onSelect = onTextureSelected,
                            label = { stringResource(it.labelRes) },
                            size = SegmentedToggleSize.Compact,
                        )
                    }
                    DescriptorRow(label = stringResource(R.string.stool_colour)) {
                        StoolColourPicker(selected = stool?.colour, onSelect = onColourSelected)
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptorRow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolSectionPreview() {
    WalkLoggerTheme {
        StoolSection(
            stool = Stool(passed = true, quantity = StoolQuantity.NORMAL, texture = StoolTexture.FIRM, colour = StoolColour.MEDIUM_BROWN),
            onPassedSelected = {},
            onQuantitySelected = {},
            onTextureSelected = {},
            onColourSelected = {},
            onClear = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolSectionUnansweredPreview() {
    WalkLoggerTheme {
        StoolSection(
            stool = null,
            onPassedSelected = {},
            onQuantitySelected = {},
            onTextureSelected = {},
            onColourSelected = {},
            onClear = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
