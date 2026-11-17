package com.example.walklogger.feature.logwalk.finishwalk

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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppConfirmDialog
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.LocationPermissionRequest
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.component.rememberAllowLocation
import com.example.walklogger.core.ui.message.UserMessageEffect
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.WalkFormActions
import com.example.walklogger.feature.logwalk.component.WalkFormContent
import com.example.walklogger.feature.logwalk.component.messageRes
import com.example.walklogger.feature.logwalk.finishwalk.component.FinishWalkSummaryCard
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData
import java.time.LocalDate
import java.time.LocalTime

@Suppress("ktlint:compose:parameter-naming")
@Composable
fun FinishWalkRoute(
    walkId: Long,
    onSaved: () -> Unit,
    onDiscarded: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: FinishWalkViewModel =
        hiltViewModel<FinishWalkViewModel, FinishWalkViewModel.Factory> { it.create(walkId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnSaved by rememberUpdatedState(onSaved)
    val currentOnNotFound by rememberUpdatedState(onNotFound)
    val currentOnDiscarded by rememberUpdatedState(onDiscarded)
    LaunchedEffect(uiState.isSaved, uiState.isNotFound, uiState.isDiscarded) {
        when {
            uiState.isSaved -> currentOnSaved()
            uiState.isDiscarded -> currentOnDiscarded()
            uiState.isNotFound -> currentOnNotFound()
        }
    }
    LocationPermissionRequest(
        shouldRequest = uiState.form.weather.shouldRequestLocationPermission,
        onResult = viewModel.formActions::onLocationPermissionResult,
    )
    LifecycleResumeEffect(viewModel) {
        viewModel.formActions.refreshLocationPermission()
        onPauseOrDispose {}
    }
    val allowLocation = rememberAllowLocation(
        wasRequested = uiState.form.weather.wasLocationRequested,
        onRequest = viewModel.formActions::requestLocationPermission,
    )
    UserMessageEffect(
        message = uiState.userMessage,
        snackbarHostState = snackbarHostState,
        onConsume = viewModel::userMessageShown,
    )
    FinishWalkScreen(
        uiState = uiState,
        formActions = viewModel.formActions,
        onAllowLocation = allowLocation,
        onToggleEditTimes = viewModel::toggleEditTimes,
        onToggleEditHandlers = viewModel::toggleEditHandlers,
        onHandlerToggle = viewModel::toggleHandler,
        onAddHandler = viewModel::addHandler,
        onDiscard = viewModel::discard,
        onDateChange = viewModel::setDate,
        onStartTimeChange = viewModel::setStartTime,
        onEndTimeChange = viewModel::setEndTime,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
fun FinishWalkScreen(
    uiState: FinishWalkUiState,
    formActions: WalkFormActions,
    onAllowLocation: () -> Unit,
    onToggleEditTimes: () -> Unit,
    onToggleEditHandlers: () -> Unit,
    onHandlerToggle: (String) -> Unit,
    onAddHandler: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.finish_walk_title),
            navigationIcon = {
                HeaderIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.walk_detail_discard),
                    onClick = { showDiscardConfirm = true },
                    enabled = uiState.canDiscard,
                )
            },
            actions = {
                Text(
                    text = stringResource(R.string.finish_walk_auto_logged),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.contentMuted,
                )
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg)
                .padding(bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            FinishWalkSummaryCard(
                dogName = uiState.dogName,
                handlers = uiState.handlers,
                selectedHandlers = uiState.selectedHandlers,
                isEditingHandlers = uiState.isEditingHandlers,
                onToggleEditHandlers = onToggleEditHandlers,
                onHandlerToggle = onHandlerToggle,
                onAddHandler = onAddHandler,
                times = uiState.times,
                isEditingTimes = uiState.isEditingTimes,
                onToggleEditTimes = onToggleEditTimes,
                onDateChange = onDateChange,
                onStartTimeChange = onStartTimeChange,
                onEndTimeChange = onEndTimeChange,
                weather = uiState.form.weather,
                weatherActions = formActions,
                onAllowLocation = onAllowLocation,
                enabled = !uiState.isLoading,
            )
            WalkFormContent(form = uiState.form, actions = formActions)
            Column(
                modifier = Modifier.padding(top = AppSpacing.md),
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
    if (showDiscardConfirm) {
        AppConfirmDialog(
            title = stringResource(R.string.walk_detail_discard_confirm_title),
            message = stringResource(R.string.walk_detail_discard_confirm_message),
            confirmLabel = stringResource(R.string.walk_detail_discard_confirm),
            onConfirm = {
                showDiscardConfirm = false
                onDiscard()
            },
            onDismiss = { showDiscardConfirm = false },
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun FinishWalkScreenPreview() {
    WalkLoggerTheme {
        FinishWalkScreen(
            uiState = LogWalkPreviewData.finishWalk,
            formActions = LogWalkPreviewData.formActions,
            onAllowLocation = {},
            onToggleEditTimes = {},
            onToggleEditHandlers = {},
            onHandlerToggle = {},
            onAddHandler = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onSave = {},
            onDiscard = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun FinishWalkScreenEditingTimesPreview() {
    WalkLoggerTheme {
        FinishWalkScreen(
            uiState = LogWalkPreviewData.finishWalk.copy(isEditingTimes = true),
            formActions = LogWalkPreviewData.formActions,
            onAllowLocation = {},
            onToggleEditTimes = {},
            onToggleEditHandlers = {},
            onHandlerToggle = {},
            onAddHandler = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onSave = {},
            onDiscard = {},
        )
    }
}
