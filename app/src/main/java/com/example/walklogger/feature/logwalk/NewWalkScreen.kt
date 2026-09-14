package com.example.walklogger.feature.logwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate

/** Start walk is a no-op until the timer flow exists. */
@Composable
fun NewWalkRoute(
    date: LocalDate,
    onBack: () -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewWalkViewModel = hiltViewModel<NewWalkViewModel, NewWalkViewModel.Factory> { it.create(date) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NewWalkScreen(
        uiState = uiState,
        onBack = onBack,
        onHandlerToggled = viewModel::toggleHandler,
        onAddHandler = viewModel::addHandler,
        onEquipmentSelected = viewModel::selectEquipment,
        onStartWalk = {},
        onLogPastWalk = { onLogPastWalk(uiState.date) },
        modifier = modifier,
    )
}

@Composable
fun NewWalkScreen(
    uiState: NewWalkUiState,
    onBack: () -> Unit,
    onHandlerToggled: (String) -> Unit,
    onAddHandler: (String) -> Unit,
    onEquipmentSelected: (Equipment) -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        ) {
            FormSection(label = stringResource(R.string.new_walk_who_is_walking)) {
                HandlerChips(
                    handlers = uiState.handlers,
                    selected = uiState.selectedHandlers,
                    onToggle = onHandlerToggled,
                    onAdd = { showAddHandler = true },
                )
            }
            FormSection(label = stringResource(R.string.new_walk_equipment)) {
                SegmentedToggle(
                    options = Equipment.entries,
                    selected = uiState.equipment,
                    onSelect = onEquipmentSelected,
                    label = { stringResource(it.labelRes) },
                )
            }
            AfterWalkNote()
            if (uiState.isToday) {
                StartWalkButton(summary = uiState.summary(), enabled = uiState.canStart, onClick = onStartWalk)
            }
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

/** Selected handlers and equipment, or why the walk can't start yet. */
@Composable
private fun NewWalkUiState.summary(): String {
    if (!canStart) return stringResource(R.string.new_walk_choose_handler)
    val locale = LocalConfiguration.current.locales[0]
    val equipmentLabel = stringResource(equipment.labelRes).lowercase(locale)
    val handlers = handlers.filter { it in selectedHandlers }
    return stringResource(R.string.new_walk_summary, handlers.joinToString(), equipmentLabel)
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NewWalkScreenPreview() {
    WalkLoggerTheme {
        NewWalkScreen(
            uiState = LogWalkPreviewData.newWalk,
            onBack = {},
            onHandlerToggled = {},
            onAddHandler = {},
            onEquipmentSelected = {},
            onStartWalk = {},
            onLogPastWalk = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NewWalkScreenPastDayPreview() {
    WalkLoggerTheme {
        NewWalkScreen(
            uiState = LogWalkPreviewData.newWalk.copy(isToday = false),
            onBack = {},
            onHandlerToggled = {},
            onAddHandler = {},
            onEquipmentSelected = {},
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
            onHandlerToggled = {},
            onAddHandler = {},
            onEquipmentSelected = {},
            onStartWalk = {},
            onLogPastWalk = {},
        )
    }
}
