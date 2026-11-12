package com.example.walklogger.feature.logwalk.pastwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.WalkFormActions
import com.example.walklogger.feature.logwalk.WeatherStatus
import com.example.walklogger.feature.logwalk.component.LocationPermissionRequest
import com.example.walklogger.feature.logwalk.component.AddHandlerDialog
import com.example.walklogger.feature.logwalk.component.FormSection
import com.example.walklogger.feature.logwalk.component.SelectableChip
import com.example.walklogger.feature.logwalk.component.WalkFormContent
import com.example.walklogger.feature.logwalk.component.WhenSection
import com.example.walklogger.feature.logwalk.component.messageRes
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData
import java.time.LocalDate
import java.time.LocalTime



@Composable
fun LogPastWalkRoute(
    date: LocalDate,
    walkId: Long?,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    onNotFound: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogPastWalkViewModel =
        hiltViewModel<LogPastWalkViewModel, LogPastWalkViewModel.Factory> { it.create(date, walkId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isSaved, uiState.isNotFound) {
        when {
            uiState.isSaved -> onSaved()
            uiState.isNotFound -> onNotFound()
        }
    }
    LocationPermissionRequest(
        shouldRequest = uiState.form.shouldRequestLocationPermission,
        onResult = viewModel.formActions::onLocationPermissionResult,
    )
    LogPastWalkScreen(
        uiState = uiState,
        formActions = viewModel.formActions,
        onClose = onClose,
        onDateChange = viewModel::setDate,
        onStartTimeChange = viewModel::setStartTime,
        onEndTimeChange = viewModel::setEndTime,
        onHandlerToggled = viewModel::toggleHandler,
        onAddHandler = viewModel::addHandler,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
fun LogPastWalkScreen(
    uiState: LogPastWalkUiState,
    formActions: WalkFormActions,
    onClose: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onHandlerToggled: (String) -> Unit,
    onAddHandler: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddHandler by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(if (uiState.isEditing) R.string.edit_walk_title else R.string.log_past_walk_title),
            navigationIcon = {
                HeaderIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.close),
                    onClick = onClose,
                )
            },
            actions = { DogBadge(name = uiState.dogName) },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        ) {
            FormSection(label = stringResource(R.string.log_past_walk_when)) {
                WhenSection(
                    date = uiState.date,
                    maxDate = uiState.maxDate,
                    startTime = uiState.startTime,
                    endTime = uiState.endTime,
                    durationMinutes = uiState.durationMinutes,
                    onDateChange = onDateChange,
                    onStartTimeChange = onStartTimeChange,
                    onEndTimeChange = onEndTimeChange,
                )
            }
            FormSection(label = stringResource(R.string.log_past_walk_handler)) {
                SelectableChip(
                    items = uiState.handlers,
                    isSelected = { it in uiState.selectedHandlers },
                    label = { it },
                    onToggle = onHandlerToggled,
                    addLabel = stringResource(R.string.new_walk_add_handler),
                    onAdd = { showAddHandler = true },
                )
            }
            WalkFormContent(form = uiState.form, actions = formActions)
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppButton(
                    text = stringResource(R.string.log_past_walk_save),
                    onClick = onSave,
                    enabled = uiState.canSave || uiState.isSaving,
                    loading = uiState.isSaving,
                )
                uiState.saveBlocker?.let { blocker ->
                    Text(
                        text = stringResource(blocker.messageRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.contentMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
    if (showAddHandler) {
        AddHandlerDialog(
            onConfirm = { name ->
                onAddHandler(name)
                showAddHandler = false
            },
            onDismiss = { showAddHandler = false },
        )
    }
}

@Composable
private fun LogPastWalkScreenPreview(uiState: LogPastWalkUiState) {
    WalkLoggerTheme {
        LogPastWalkScreen(
            uiState = uiState,
            formActions = LogWalkPreviewData.formActions,
            onClose = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onHandlerToggled = {},
            onAddHandler = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LogPastWalkScreenDefaultPreview() = LogPastWalkScreenPreview(LogWalkPreviewData.logPastWalk)

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LogPastWalkScreenCompletePreview() {
    val walk = LogWalkPreviewData.logPastWalk
    LogPastWalkScreenPreview(walk.copy(form = walk.form.copy(rating = 5, notes = "Calm on the lead, one barking dog at the gate.")))
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LogPastWalkScreenEditPreview() {
    val walk = LogWalkPreviewData.logPastWalk
    LogPastWalkScreenPreview(walk.copy(isEditing = true, form = walk.form.copy(rating = 5, notes = "Calm on the lead.")))
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LogPastWalkScreenEmptyPreview() {
    val walk = LogWalkPreviewData.logPastWalk
    LogPastWalkScreenPreview(
        walk.copy(
            startTime = null,
            endTime = null,
            selectedHandlers = emptySet(),
            form = walk.form.copy(
                temperatureInput = "",
                condition = null,
                weatherStatus = WeatherStatus.IDLE,
                isWeatherExpanded = false,
                selectedBehaviourIssueIds = emptySet(),
                isBehaviourIssuesExpanded = false,
                selectedMedicalConcernIds = emptySet(),
                isMedicalConcernsExpanded = false,
            ),
        ),
    )
}
