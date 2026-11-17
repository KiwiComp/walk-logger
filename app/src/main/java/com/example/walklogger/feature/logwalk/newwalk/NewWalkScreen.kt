package com.example.walklogger.feature.logwalk.newwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.LocationPermissionRequest
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.message.UserMessageEffect
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.AddHandlerDialog
import com.example.walklogger.feature.logwalk.component.FormSection
import com.example.walklogger.feature.logwalk.component.SelectableChip
import com.example.walklogger.feature.logwalk.component.labelRes
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData
import java.time.LocalDate

@Suppress("ktlint:compose:parameter-naming")
@Composable
fun NewWalkRoute(
    date: LocalDate,
    onBack: () -> Unit,
    onWalkStarted: (walkId: Long) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: NewWalkViewModel =
        hiltViewModel<NewWalkViewModel, NewWalkViewModel.Factory> { it.create(date) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnWalkStarted by rememberUpdatedState(onWalkStarted)
    LaunchedEffect(uiState.startedWalkId) {
        uiState.startedWalkId?.let(currentOnWalkStarted)
    }
    LocationPermissionRequest(
        shouldRequest = uiState.shouldRequestLocationPermission,
        onResult = { viewModel.onLocationPermissionAnswered() },
    )
    UserMessageEffect(
        message = uiState.userMessage,
        snackbarHostState = snackbarHostState,
        onConsume = viewModel::userMessageShown,
    )
    NewWalkScreen(
        uiState = uiState,
        onBack = onBack,
        onHandlerToggle = viewModel::toggleHandler,
        onAddHandler = viewModel::addHandler,
        onEquipmentSelect = viewModel::selectEquipment,
        onStartWalk = viewModel::startWalk,
        onLogPastWalk = { onLogPastWalk(uiState.date) },
        modifier = modifier,
    )
}

@Composable
fun NewWalkScreen(
    uiState: NewWalkUiState,
    onBack: () -> Unit,
    onHandlerToggle: (String) -> Unit,
    onAddHandler: (String) -> Unit,
    onEquipmentSelect: (Equipment) -> Unit,
    onStartWalk: () -> Unit,
    onLogPastWalk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddHandler by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.new_walk_title),
            navigationIcon = {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.back),
                    onClick = onBack,
                )
            },
            actions = { DogBadge(name = uiState.dogName) },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg)
                .padding(top = AppSpacing.md, bottom = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            FormSection(label = stringResource(R.string.new_walk_who_is_walking)) {
                SelectableChip(
                    items = uiState.handlers,
                    isSelected = { it in uiState.selectedHandlers },
                    label = { it },
                    onToggle = onHandlerToggle,
                    addLabel = stringResource(R.string.new_walk_add_handler),
                    onAdd = { showAddHandler = true },
                )
            }
            FormSection(label = stringResource(R.string.new_walk_equipment)) {
                SegmentedToggle(
                    options = Equipment.entries,
                    selected = uiState.equipment,
                    onSelect = onEquipmentSelect,
                    label = { stringResource(it.labelRes) },
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg)
                .padding(bottom = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StartWalkButton(
                subtitle = stringResource(
                    if (uiState.canStart) {
                        R.string.new_walk_timer_starts
                    } else {
                        R.string.new_walk_choose_handler
                    },
                ),
                enabled = uiState.canStart,
                onClick = onStartWalk,
            )
            Text(
                text = stringResource(R.string.new_walk_after_note),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.contentMuted,
                textAlign = TextAlign.Center,
            )
            LogPastWalkButton(onClick = onLogPastWalk)
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NewWalkScreenPreview() {
    WalkLoggerTheme {
        NewWalkScreen(
            uiState = LogWalkPreviewData.newWalk,
            onBack = {},
            onHandlerToggle = {},
            onAddHandler = {},
            onEquipmentSelect = {},
            onStartWalk = {},
            onLogPastWalk = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NewWalkScreenNoHandlerPreview() {
    WalkLoggerTheme {
        NewWalkScreen(
            uiState = LogWalkPreviewData.newWalk.copy(selectedHandlers = emptySet()),
            onBack = {},
            onHandlerToggle = {},
            onAddHandler = {},
            onEquipmentSelect = {},
            onStartWalk = {},
            onLogPastWalk = {},
        )
    }
}
