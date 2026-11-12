package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.WalkFormActions
import com.example.walklogger.feature.logwalk.WalkFormState
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData

private const val NOTES_MIN_LINES = 3

/** The sections the after-walk and past-walk forms share, from equipment to notes. */
@Composable
internal fun WalkFormContent(
    form: WalkFormState,
    actions: WalkFormActions,
    modifier: Modifier = Modifier,
) {
    var showAddIssue by rememberSaveable { mutableStateOf(false) }
    var showAddConcern by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
    ) {
        FormSection(label = stringResource(R.string.new_walk_equipment)) {
            SegmentedToggle(
                options = Equipment.entries,
                selected = form.equipment,
                onSelect = actions::selectEquipment,
                label = { stringResource(it.labelRes) },
            )
        }
        FormSection(
            label = stringResource(R.string.log_past_walk_rating),
            hint = stringResource(R.string.log_past_walk_rating_scale),
        ) {
            RatingScale(rating = form.rating, onRatingChange = actions::setRating)
        }
        StoolSection(
            stool = form.stool,
            onPassedSelect = actions::selectStoolPassed,
            onQuantitySelect = actions::selectStoolQuantity,
            onTextureSelect = actions::selectStoolTexture,
            onColourSelect = actions::selectStoolColour,
            onClear = actions::clearStool,
            expanded = form.isStoolExpanded,
            onFold = actions::foldStool,
            onExpand = actions::expandStool,
        )
        OptionalRatingsSection(
            ratings = form.optionalRatings,
            expanded = form.isOptionalRatingsExpanded,
            onToggle = actions::toggleOptionalRatings,
            onClear = actions::clearOptionalRatings,
            onRatingChange = actions::setOptionalRating,
        )
        WeatherSection(form = form, actions = actions)
        BehaviourSection(
            issues = form.behaviourIssues,
            selectedIds = form.selectedBehaviourIssueIds,
            expanded = form.isBehaviourIssuesExpanded,
            onToggle = actions::toggleBehaviourIssuesExpanded,
            onClear = actions::clearBehaviourIssues,
            onIssueToggle = actions::toggleBehaviourIssue,
            onAddIssue = { showAddIssue = true },
        )
        MedicalSection(
            concerns = form.medicalConcerns,
            selectedIds = form.selectedMedicalConcernIds,
            expanded = form.isMedicalConcernsExpanded,
            onToggle = actions::toggleMedicalConcernsExpanded,
            onClear = actions::clearMedicalConcerns,
            onConcernToggle = actions::toggleMedicalConcern,
            onAddConcern = { showAddConcern = true },
        )
        FormSection(label = stringResource(R.string.log_past_walk_notes)) {
            AppTextField(
                value = form.notes,
                onValueChange = actions::setNotes,
                placeholder = stringResource(R.string.log_past_walk_notes_placeholder),
                singleLine = false,
                minLines = NOTES_MIN_LINES,
            )
        }
    }
    if (showAddIssue) {
        AddBehaviourIssueDialog(
            onConfirm = { name ->
                actions.addBehaviourIssue(name)
                showAddIssue = false
            },
            onDismiss = { showAddIssue = false },
        )
    }
    if (showAddConcern) {
        AddMedicalConcernDialog(
            onConfirm = { name ->
                actions.addMedicalConcern(name)
                showAddConcern = false
            },
            onDismiss = { showAddConcern = false },
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun WalkFormContentPreview() {
    WalkLoggerTheme {
        WalkFormContent(
            form = LogWalkPreviewData.finishWalk.form,
            actions = LogWalkPreviewData.formActions,
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
