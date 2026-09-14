package com.example.walklogger.feature.logwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppDateField
import com.example.walklogger.core.ui.component.AppPickerField
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val DateWeight = 3f
private const val TimeWeight = 2f
private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM")
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Date, start and end pickers side by side, with the resulting duration underneath. */
@Composable
internal fun WhenSection(
    date: LocalDate,
    maxDate: LocalDate,
    startTime: LocalTime?,
    endTime: LocalTime?,
    durationMinutes: Int?,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            AppDateField(
                date = date,
                onDateChange = { if (it != null) onDateChange(it) },
                label = stringResource(R.string.log_past_walk_date),
                formatter = DateFormatter,
                maxDate = maxDate,
                modifier = Modifier.weight(DateWeight),
            )
            TimeField(
                time = startTime,
                label = stringResource(R.string.log_past_walk_start),
                dialogTitle = stringResource(R.string.log_past_walk_start_time),
                onTimeChange = onStartTimeChange,
                modifier = Modifier.weight(TimeWeight),
            )
            TimeField(
                time = endTime,
                label = stringResource(R.string.log_past_walk_end),
                dialogTitle = stringResource(R.string.log_past_walk_end_time),
                onTimeChange = onEndTimeChange,
                modifier = Modifier.weight(TimeWeight),
            )
        }
        if (durationMinutes != null) {
            Text(
                text = pluralStringResource(R.plurals.log_past_walk_duration_minutes, durationMinutes, durationMinutes),
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted,
            )
        }
    }
}

/** Opens a dial picker; the keyboard toggle switches it to typed input. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    time: LocalTime?,
    label: String,
    dialogTitle: String,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    AppPickerField(
        value = time?.format(TimeFormatter).orEmpty(),
        label = label,
        onPress = { showPicker = true },
        modifier = modifier,
    )
    if (showPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = time?.hour ?: LocalTime.NOON.hour,
            initialMinute = time?.minute ?: 0,
            is24Hour = true
        )
        var displayMode by remember { mutableStateOf(TimePickerDisplayMode.Picker) }
        TimePickerDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(dialogTitle) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(LocalTime.of(pickerState.hour, pickerState.minute))
                        showPicker = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            modeToggleButton = {
                TimePickerDialogDefaults.DisplayModeToggle(
                    displayMode = displayMode,
                    onDisplayModeChange = {
                        displayMode = if (displayMode == TimePickerDisplayMode.Picker) {
                            TimePickerDisplayMode.Input
                        } else {
                            TimePickerDisplayMode.Picker
                        }
                    },
                )
            },
        ) {
            if (displayMode == TimePickerDisplayMode.Picker) {
                TimePicker(state = pickerState)
            } else {
                TimeInput(state = pickerState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WhenSectionPreview() {
    WalkLoggerTheme {
        WhenSection(
            date = LocalDate.of(2026, 9, 12),
            maxDate = LocalDate.of(2026, 9, 14),
            startTime = LocalTime.of(17, 40),
            endTime = LocalTime.of(18, 5),
            durationMinutes = 25,
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WhenSectionEmptyPreview() {
    WalkLoggerTheme {
        WhenSection(
            date = LocalDate.of(2026, 9, 12),
            maxDate = LocalDate.of(2026, 9, 14),
            startTime = null,
            endTime = null,
            durationMinutes = null,
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
