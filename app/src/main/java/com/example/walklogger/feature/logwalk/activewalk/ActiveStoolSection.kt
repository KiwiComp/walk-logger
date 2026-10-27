package com.example.walklogger.feature.logwalk.activewalk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.ui.component.AppSwitch
import com.example.walklogger.core.ui.component.AppSwitchStyle
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.FormStyle
import com.example.walklogger.feature.logwalk.component.StoolDescriptors
import com.example.walklogger.feature.logwalk.component.StoolSummary

/**
 * The timer screen's stool section: a switch that is on once the dog has passed stool, with the
 * description rows below it. Done folds the rows into a summary line and turns into Edit; Edit or
 * the summary line opens them again.
 * Off means not answered; No is only offered in the after-walk form.
 */
@Composable
internal fun ActiveStoolSection(
    stool: Stool?,
    expanded: Boolean,
    onPassedChange: (Boolean) -> Unit,
    onQuantitySelected: (StoolQuantity) -> Unit,
    onTextureSelected: (StoolTexture) -> Unit,
    onColourSelected: (StoolColour?) -> Unit,
    onFold: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passed = stool?.passed == true
    val label = stringResource(R.string.log_past_walk_stool)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(
                text = label,
                color = AppTheme.colors.onHeaderMuted,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.weight(1f))
            if (passed) {
                SectionLabel(
                    text = stringResource(if (expanded) R.string.stool_done else R.string.stool_edit),
                    color = AppTheme.colors.onHeader,
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = if (expanded) onFold else onExpand)
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                )
            }
            AppSwitch(
                checked = passed,
                onCheckedChange = onPassedChange,
                style = AppSwitchStyle.Inverse,
                modifier = Modifier.semantics { contentDescription = label },
            )
        }
        AnimatedVisibility(visible = passed) {
            Column {
                Spacer(Modifier.height(AppSpacing.sm))
                if (expanded) {
                    StoolDescriptors(
                        stool = stool,
                        onQuantitySelected = onQuantitySelected,
                        onTextureSelected = onTextureSelected,
                        onColourSelected = onColourSelected,
                        style = FormStyle.Inverse,
                    )
                } else {
                    StoolSummary(stool = stool, onClick = onExpand, style = FormStyle.Inverse)
                }
            }
        }
    }
}

@Preview
@Composable
private fun ActiveStoolSectionOffPreview() {
    WalkLoggerTheme {
        ActiveStoolSectionPreview(stool = null, expanded = false)
    }
}

@Preview
@Composable
private fun ActiveStoolSectionExpandedPreview() {
    WalkLoggerTheme {
        ActiveStoolSectionPreview(stool = Stool(passed = true, quantity = StoolQuantity.NORMAL), expanded = true)
    }
}

@Preview
@Composable
private fun ActiveStoolSectionFoldedPreview() {
    WalkLoggerTheme {
        ActiveStoolSectionPreview(
            stool = Stool(passed = true, quantity = StoolQuantity.NORMAL, texture = StoolTexture.FIRM, colour = StoolColour.MEDIUM_BROWN),
            expanded = false,
        )
    }
}

@Composable
private fun ActiveStoolSectionPreview(stool: Stool?, expanded: Boolean) {
    ActiveStoolSection(
        stool = stool,
        expanded = expanded,
        onPassedChange = {},
        onQuantitySelected = {},
        onTextureSelected = {},
        onColourSelected = {},
        onFold = {},
        onExpand = {},
        modifier = Modifier.background(AppTheme.colors.header).padding(AppSpacing.lg),
    )
}
