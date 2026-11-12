package com.example.walklogger.feature.logwalk.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * "Stool" form section. It is never folded away: [stool] stays null until the handler actively picks
 * Yes or No, and Clear returns it to null. The description rows only show after Yes; Done folds them into
 * a summary line and turns into Edit, which opens them again.
 */
@Composable
internal fun StoolSection(
    stool: Stool?,
    onPassedSelect: (Boolean) -> Unit,
    onQuantitySelect: (StoolQuantity) -> Unit,
    onTextureSelect: (StoolTexture) -> Unit,
    onColourSelect: (StoolColour?) -> Unit,
    onClear: () -> Unit,
    expanded: Boolean,
    onFold: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passed = stool?.passed == true
    FormSection(
        label = stringResource(R.string.log_past_walk_stool),
        actionLabel = if (stool != null) stringResource(R.string.form_section_clear) else null,
        onAction = onClear,
        leadingActionLabel = if (passed) {
            stringResource(
                if (expanded) R.string.stool_done else R.string.stool_edit,
            )
        } else {
            null
        },
        onLeadingAction = if (expanded) onFold else onExpand,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            SegmentedToggle(
                options = listOf(true, false),
                selected = stool?.passed,
                onSelect = onPassedSelect,
                label = {
                    stringResource(if (it) R.string.stool_passed_yes else R.string.stool_passed_no)
                },
            )
            AnimatedVisibility(visible = passed) {
                if (expanded) {
                    StoolDescriptors(
                        stool = stool,
                        onQuantitySelect = onQuantitySelect,
                        onTextureSelect = onTextureSelect,
                        onColourSelect = onColourSelect,
                    )
                } else {
                    StoolSummary(stool = stool, onClick = onExpand)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolSectionPreview() {
    WalkLoggerTheme {
        StoolSection(
            stool = Stool(
                passed = true,
                quantity = StoolQuantity.NORMAL,
                texture = StoolTexture.FIRM,
                colour = StoolColour.MEDIUM_BROWN,
            ),
            onPassedSelect = {},
            onQuantitySelect = {},
            onTextureSelect = {},
            onColourSelect = {},
            onClear = {},
            expanded = true,
            onFold = {},
            onExpand = {},
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
            onPassedSelect = {},
            onQuantitySelect = {},
            onTextureSelect = {},
            onColourSelect = {},
            onClear = {},
            expanded = true,
            onFold = {},
            onExpand = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolSectionFoldedPreview() {
    WalkLoggerTheme {
        StoolSection(
            stool = Stool(
                passed = true,
                quantity = StoolQuantity.NORMAL,
                texture = StoolTexture.FIRM,
                colour = StoolColour.MEDIUM_BROWN,
            ),
            onPassedSelect = {},
            onQuantitySelect = {},
            onTextureSelect = {},
            onColourSelect = {},
            onClear = {},
            expanded = false,
            onFold = {},
            onExpand = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
