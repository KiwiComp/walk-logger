package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData

/** Collapsible "Behaviour" form section; issues are optional so it starts folded away. */
@Composable
internal fun BehaviourSection(
    issues: List<BehaviourIssue>,
    selectedIds: Set<Long>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit,
    onIssueToggle: (BehaviourIssue) -> Unit,
    onAddIssue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormSection(
        label = stringResource(R.string.log_past_walk_behaviour),
        expanded = expanded,
        onToggle = onToggle,
        actionLabel = if (selectedIds.isNotEmpty()) {
            stringResource(
                R.string.form_section_clear,
            )
        } else {
            null
        },
        onAction = onClear,
        modifier = modifier,
    ) {
        SelectableChip(
            items = issues,
            isSelected = { it.id in selectedIds },
            label = { it.label() },
            onToggle = onIssueToggle,
            addLabel = stringResource(R.string.behaviour_other_issue),
            onAdd = onAddIssue,
            size = SelectableChipSize.Compact,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BehaviourSectionPreview() {
    WalkLoggerTheme {
        BehaviourSection(
            issues = LogWalkPreviewData.behaviourIssues,
            selectedIds = setOf(1L, 4L),
            expanded = true,
            onToggle = {},
            onClear = {},
            onIssueToggle = {},
            onAddIssue = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
