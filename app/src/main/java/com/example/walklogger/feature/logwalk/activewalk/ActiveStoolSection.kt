package com.example.walklogger.feature.logwalk.activewalk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.ui.component.AppSwitch
import com.example.walklogger.core.ui.component.EditDonePillButton
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.StoolDescriptors
import com.example.walklogger.feature.logwalk.component.StoolSummary

/**
 * The timer screen's stool card: a switch that is on once the dog has passed stool, with the
 * description rows below it. Done folds the rows into a summary line and turns into Edit, which opens
 * them again.
 * Off means not answered; No is only offered in the after-walk form.
 */
@Composable
internal fun ActiveStoolSection(
    stool: Stool?,
    expanded: Boolean,
    onPassedChange: (Boolean) -> Unit,
    onQuantitySelect: (StoolQuantity) -> Unit,
    onTextureSelect: (StoolTexture) -> Unit,
    onColourSelect: (StoolColour?) -> Unit,
    onFold: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passed = stool?.passed == true
    val label = stringResource(R.string.log_past_walk_stool)
    val shape = RoundedCornerShape(AppRadius.lg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.card)
            .border(1.dp, AppTheme.colors.outline, shape)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.content,
                modifier = Modifier.weight(1f),
            )
            if (passed) {
                EditDonePillButton(
                    isEditing = expanded,
                    editLabel = stringResource(R.string.stool_edit),
                    doneLabel = stringResource(R.string.stool_done),
                    onClick = if (expanded) onFold else onExpand,
                )
            }
            AppSwitch(
                checked = passed,
                onCheckedChange = onPassedChange,
                modifier = Modifier.semantics { contentDescription = label },
            )
        }
        AnimatedVisibility(visible = passed) {
            Column {
                Spacer(Modifier.height(AppSpacing.md))
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
        ActiveStoolSectionPreview(
            stool = Stool(passed = true, quantity = StoolQuantity.NORMAL),
            expanded = true,
        )
    }
}

@Preview
@Composable
private fun ActiveStoolSectionFoldedPreview() {
    WalkLoggerTheme {
        ActiveStoolSectionPreview(
            stool = Stool(
                passed = true,
                quantity = StoolQuantity.NORMAL,
                texture = StoolTexture.FIRM,
                colour = StoolColour.MEDIUM_BROWN,
            ),
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
        onQuantitySelect = {},
        onTextureSelect = {},
        onColourSelect = {},
        onFold = {},
        onExpand = {},
        modifier = Modifier.padding(AppSpacing.lg),
    )
}
