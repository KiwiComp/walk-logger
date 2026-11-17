package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppDatePickerDialog
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val IconSize = 20.dp

/** The date with Change beside it, then start → end as two tappable tiles, and the duration underneath. */
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
    val locale = LocalConfiguration.current.locales[0]
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEE d MMM", locale) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tab_calendar),
                contentDescription = null,
                tint = AppTheme.colors.contentSecondary,
                modifier = Modifier.size(IconSize),
            )
            Text(
                text = date.format(dateFormatter),
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.colors.content,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.when_change_date),
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.colors.action,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = { showDatePicker = true })
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            TimeField(
                time = startTime,
                label = stringResource(R.string.log_past_walk_start),
                dialogTitle = stringResource(R.string.log_past_walk_start_time),
                onTimeChange = onStartTimeChange,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppTheme.colors.dashed,
                modifier = Modifier.size(IconSize),
            )
            TimeField(
                time = endTime,
                label = stringResource(R.string.log_past_walk_end),
                dialogTitle = stringResource(R.string.log_past_walk_end_time),
                onTimeChange = onEndTimeChange,
                modifier = Modifier.weight(1f),
            )
        }
        if (durationMinutes != null) {
            Text(
                text = pluralStringResource(
                    R.plurals.log_past_walk_duration_minutes,
                    durationMinutes,
                    durationMinutes,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.contentMuted,
            )
        }
    }
    if (showDatePicker) {
        AppDatePickerDialog(
            date = date,
            maxDate = maxDate,
            onConfirm = {
                if (it != null) onDateChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

/** A tile with [label] over the time; it opens a dial picker whose keyboard toggle switches to typed input. */
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
    val shape = RoundedCornerShape(AppRadius.md)
    Column(
        modifier = modifier
            .clip(shape)
            .background(AppTheme.colors.card)
            .border(1.dp, AppTheme.colors.outlineStrong, shape)
            .clickable(role = Role.Button, onClick = { showPicker = true })
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.contentMuted,
        )
        Text(
            text = time?.format(TimeFormatter) ?: stringResource(R.string.when_time_unset),
            style = MaterialTheme.typography.titleMedium,
            color = if (time == null) AppTheme.colors.placeholder else AppTheme.colors.content,
        )
    }
    if (showPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = time?.hour ?: LocalTime.NOON.hour,
            initialMinute = time?.minute ?: 0,
            is24Hour = true,
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
