package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData

/** Collapsible "Medical" form section; concerns are optional so it starts folded away. */
@Composable
internal fun MedicalSection(
    concerns: List<MedicalConcern>,
    selectedIds: Set<Long>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit,
    onConcernToggled: (MedicalConcern) -> Unit,
    onAddConcern: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormSection(
        label = stringResource(R.string.log_past_walk_medical),
        expanded = expanded,
        onToggle = onToggle,
        actionLabel = if (selectedIds.isNotEmpty()) stringResource(R.string.form_section_clear) else null,
        onAction = onClear,
        modifier = modifier,
    ) {
        SelectableChip(
            items = concerns,
            isSelected = { it.id in selectedIds },
            label = { it.label() },
            onToggle = onConcernToggled,
            addLabel = stringResource(R.string.medical_other_concern),
            onAdd = onAddConcern,
            size = SelectableChipSize.Compact,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicalSectionPreview() {
    WalkLoggerTheme {
        MedicalSection(
            concerns = LogWalkPreviewData.medicalConcerns,
            selectedIds = setOf(2L, 4L),
            expanded = true,
            onToggle = {},
            onClear = {},
            onConcernToggled = {},
            onAddConcern = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
