package com.example.walklogger.feature.logwalk

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
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate
import java.time.LocalTime

private const val NotesMinLines = 3

@Composable
fun LogPastWalkRoute(
    date: LocalDate,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogPastWalkViewModel =
        hiltViewModel<LogPastWalkViewModel, LogPastWalkViewModel.Factory> { it.create(date) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }
    LogPastWalkScreen(
        uiState = uiState,
        onClose = onClose,
        onDateChange = viewModel::setDate,
        onStartTimeChange = viewModel::setStartTime,
        onEndTimeChange = viewModel::setEndTime,
        onHandlerToggled = viewModel::toggleHandler,
        onAddHandler = viewModel::addHandler,
        onRatingChange = viewModel::setRating,
        onEquipmentSelected = viewModel::selectEquipment,
        onRainSelected = viewModel::selectRain,
        onTemperatureSelected = viewModel::selectTemperature,
        onWindSelected = viewModel::selectWind,
        onNotesChange = viewModel::setNotes,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
fun LogPastWalkScreen(
    uiState: LogPastWalkUiState,
    onClose: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onHandlerToggled: (String) -> Unit,
    onAddHandler: (String) -> Unit,
    onRatingChange: (Int) -> Unit,
    onEquipmentSelected: (Equipment) -> Unit,
    onRainSelected: (Rain) -> Unit,
    onTemperatureSelected: (Temperature) -> Unit,
    onWindSelected: (Wind) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddHandler by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.log_past_walk_title),
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
                .verticalScroll(rememberScrollState())
                .imePadding()
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
                HandlerChips(
                    handlers = uiState.handlers,
                    selected = uiState.selectedHandlers,
                    onToggle = onHandlerToggled,
                    onAdd = { showAddHandler = true },
                )
            }
            FormSection(
                label = stringResource(R.string.log_past_walk_rating),
                hint = stringResource(R.string.log_past_walk_rating_scale),
            ) {
                RatingScale(rating = uiState.rating, onRatingChange = onRatingChange)
            }
            FormSection(label = stringResource(R.string.new_walk_equipment)) {
                SegmentedToggle(
                    options = Equipment.entries,
                    selected = uiState.equipment,
                    onSelect = onEquipmentSelected,
                    label = { stringResource(it.labelRes) },
                )
            }
            FormSection(label = stringResource(R.string.log_past_walk_weather)) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    WeatherRow(
                        label = stringResource(R.string.weather_rain),
                        options = Rain.entries,
                        selected = uiState.rain,
                        onSelect = onRainSelected,
                        optionLabel = { stringResource(it.labelRes) },
                    )
                    WeatherRow(
                        label = stringResource(R.string.weather_temperature),
                        options = Temperature.entries,
                        selected = uiState.temperature,
                        onSelect = onTemperatureSelected,
                        optionLabel = { stringResource(it.labelRes) },
                    )
                    WeatherRow(
                        label = stringResource(R.string.weather_wind),
                        options = Wind.entries,
                        selected = uiState.wind,
                        onSelect = onWindSelected,
                        optionLabel = { stringResource(it.labelRes) },
                    )
                }
            }
            FormSection(label = stringResource(R.string.log_past_walk_notes)) {
                AppTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    placeholder = stringResource(R.string.log_past_walk_notes_placeholder),
                    singleLine = false,
                    minLines = NotesMinLines,
                )
            }
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
                        color = InkMuted,
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

private val SaveBlocker.messageRes: Int
    get() = when (this) {
        SaveBlocker.HANDLER -> R.string.log_past_walk_needs_handler
        SaveBlocker.TIMES -> R.string.log_past_walk_needs_times
        SaveBlocker.END_BEFORE_START -> R.string.log_past_walk_needs_end_after_start
        SaveBlocker.IN_FUTURE -> R.string.log_past_walk_needs_past_end
        SaveBlocker.RATING -> R.string.log_past_walk_needs_rating
    }

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LogPastWalkScreenPreview() {
    WalkLoggerTheme {
        LogPastWalkScreen(
            uiState = LogWalkPreviewData.logPastWalk,
            onClose = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onHandlerToggled = {},
            onAddHandler = {},
            onRatingChange = {},
            onEquipmentSelected = {},
            onRainSelected = {},
            onTemperatureSelected = {},
            onWindSelected = {},
            onNotesChange = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LogPastWalkScreenCompletePreview() {
    WalkLoggerTheme {
        LogPastWalkScreen(
            uiState = LogWalkPreviewData.logPastWalk.copy(rating = 5, notes = "Calm on the lead, one barking dog at the gate."),
            onClose = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onHandlerToggled = {},
            onAddHandler = {},
            onRatingChange = {},
            onEquipmentSelected = {},
            onRainSelected = {},
            onTemperatureSelected = {},
            onWindSelected = {},
            onNotesChange = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LogPastWalkScreenEmptyPreview() {
    WalkLoggerTheme {
        LogPastWalkScreen(
            uiState = LogWalkPreviewData.logPastWalk.copy(startTime = null, endTime = null, selectedHandlers = emptySet(), rain = null, temperature = null, wind = null),
            onClose = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onHandlerToggled = {},
            onAddHandler = {},
            onRatingChange = {},
            onEquipmentSelected = {},
            onRainSelected = {},
            onTemperatureSelected = {},
            onWindSelected = {},
            onNotesChange = {},
            onSave = {},
        )
    }
}
