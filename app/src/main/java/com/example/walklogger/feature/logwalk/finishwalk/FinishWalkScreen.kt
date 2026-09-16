package com.example.walklogger.feature.logwalk.finishwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppButtonStyle
import com.example.walklogger.core.ui.component.AppButtonWidth
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.OnNavyMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.FormSection
import com.example.walklogger.feature.logwalk.component.RatingScale
import com.example.walklogger.feature.logwalk.component.WeatherSection
import com.example.walklogger.feature.logwalk.component.WhenSection
import com.example.walklogger.feature.logwalk.component.labelRes
import com.example.walklogger.feature.logwalk.component.messageRes
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter



private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private const val NotesMinLines = 3

@Composable
fun FinishWalkRoute(
    walkId: Long,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FinishWalkViewModel = hiltViewModel<FinishWalkViewModel, FinishWalkViewModel.Factory> { it.create(walkId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }
    FinishWalkScreen(
        uiState = uiState,
        onToggleEditTimes = viewModel::toggleEditTimes,
        onDateChange = viewModel::setDate,
        onStartTimeChange = viewModel::setStartTime,
        onEndTimeChange = viewModel::setEndTime,
        onRatingChange = viewModel::setRating,
        onEquipmentSelected = viewModel::selectEquipment,
        onRainSelected = viewModel::selectRain,
        onTemperatureSelected = viewModel::selectTemperature,
        onWindSelected = viewModel::selectWind,
        onSnowSelected = viewModel::selectSnow,
        onToggleWeather = viewModel::toggleWeather,
        onClearWeather = viewModel::clearWeather,
        onNotesChange = viewModel::setNotes,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
fun FinishWalkScreen(
    uiState: FinishWalkUiState,
    onToggleEditTimes: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onRatingChange: (Int) -> Unit,
    onEquipmentSelected: (Equipment) -> Unit,
    onRainSelected: (Rain) -> Unit,
    onTemperatureSelected: (Temperature) -> Unit,
    onWindSelected: (Wind) -> Unit,
    onSnowSelected: (Snow) -> Unit,
    onToggleWeather: () -> Unit,
    onClearWeather: () -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.finish_walk_title),
            actions = {
                AppButton(
                    text = stringResource(R.string.finish_walk_edit_times),
                    onClick = onToggleEditTimes,
                    style = AppButtonStyle.Tertiary,
                    width = AppButtonWidth.Wrap,
                    enabled = !uiState.isLoading,
                )
            },
        ) {
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                text = uiState.timesSummary(),
                style = MaterialTheme.typography.bodyMedium,
                color = OnNavyMuted,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        ) {
            if (uiState.isEditingTimes) {
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
            WeatherSection(
                rain = uiState.rain,
                temperature = uiState.temperature,
                wind = uiState.wind,
                snow = uiState.snow,
                expanded = uiState.isWeatherExpanded,
                onToggle = onToggleWeather,
                onClear = onClearWeather,
                onRainSelected = onRainSelected,
                onTemperatureSelected = onTemperatureSelected,
                onWindSelected = onWindSelected,
                onSnowSelected = onSnowSelected,
            )
            FormSection(label = stringResource(R.string.log_past_walk_notes)) {
                AppTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    placeholder = stringResource(R.string.log_past_walk_notes_placeholder),
                    singleLine = false,
                    minLines = NotesMinLines,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.walkedBy(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted,
                )
                AppButton(
                    text = stringResource(R.string.log_past_walk_save),
                    onClick = onSave,
                    width = AppButtonWidth.Wrap,
                    enabled = uiState.canSave || uiState.isSaving,
                    loading = uiState.isSaving,
                )
            }
            uiState.saveBlocker?.let { blocker ->
                Text(
                    text = stringResource(blocker.messageRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** "Sixten walked by Anna", or just "Walked by Anna" until the dog is known. */
@Composable
private fun FinishWalkUiState.walkedBy(): String {
    val handlers = handlers.joinToString()
    val dog = dogName ?: return stringResource(R.string.finish_walk_walked_by, handlers)
    return stringResource(R.string.finish_walk_dog_walked_by, dog, handlers)
}

/** "07:05 → 07:39 · 34 min · logged automatically", or nothing until the walk has loaded. */
@Composable
private fun FinishWalkUiState.timesSummary(): String {
    val start = startTime ?: return ""
    val end = endTime ?: return ""
    val minutes = durationMinutes ?: return ""
    return stringResource(
        R.string.finish_walk_times_summary,
        start.format(TimeFormatter),
        end.format(TimeFormatter),
        stringResource(R.string.walk_duration_minutes, minutes),
    )
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun FinishWalkScreenPreview() {
    WalkLoggerTheme {
        FinishWalkScreen(
            uiState = LogWalkPreviewData.finishWalk,
            onToggleEditTimes = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onRatingChange = {},
            onEquipmentSelected = {},
            onRainSelected = {},
            onTemperatureSelected = {},
            onWindSelected = {},
            onSnowSelected = {},
            onToggleWeather = {},
            onClearWeather = {},
            onNotesChange = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun FinishWalkScreenEditingTimesPreview() {
    WalkLoggerTheme {
        FinishWalkScreen(
            uiState = LogWalkPreviewData.finishWalk.copy(isEditingTimes = true),
            onToggleEditTimes = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onRatingChange = {},
            onEquipmentSelected = {},
            onRainSelected = {},
            onTemperatureSelected = {},
            onWindSelected = {},
            onSnowSelected = {},
            onToggleWeather = {},
            onClearWeather = {},
            onNotesChange = {},
            onSave = {},
        )
    }
}
