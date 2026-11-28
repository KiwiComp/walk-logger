package com.example.walklogger.feature.logwalk.finishwalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.EditDonePillButton
import com.example.walklogger.core.ui.component.PillButton
import com.example.walklogger.core.ui.component.PillButtonStyle
import com.example.walklogger.core.ui.component.durationWithDistance
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.WalkTimesState
import com.example.walklogger.feature.logwalk.WeatherFormActions
import com.example.walklogger.feature.logwalk.WeatherFormState
import com.example.walklogger.feature.logwalk.component.AddHandlerDialog
import com.example.walklogger.feature.logwalk.component.SelectableChip
import com.example.walklogger.feature.logwalk.component.WeatherContent
import com.example.walklogger.feature.logwalk.component.WhenSection
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val RowIconSize = 20.dp
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Top card of the after-walk form: who walked (Change opens the handler chips), when (Edit opens the
 * pickers) and the weather (the pencil opens its manual fields).
 */
@Composable
internal fun FinishWalkSummaryCard(
    dogName: String?,
    handlers: List<String>,
    selectedHandlers: Set<String>,
    isEditingHandlers: Boolean,
    onToggleEditHandlers: () -> Unit,
    onHandlerToggle: (String) -> Unit,
    onAddHandler: (String) -> Unit,
    times: WalkTimesState,
    distanceMeters: Double?,
    isEditingTimes: Boolean,
    onToggleEditTimes: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    weather: WeatherFormState,
    weatherActions: WeatherFormActions,
    onAllowLocation: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var showAddHandler by rememberSaveable { mutableStateOf(false) }
    val shape = RoundedCornerShape(AppRadius.lg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.card)
            .border(1.dp, AppTheme.colors.outline, shape)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RowIcon(painterResource(R.drawable.ic_common_dog))
            Column(modifier = Modifier.weight(1f).padding(start = AppSpacing.md)) {
                dogName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.content,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.finish_walk_walked_by,
                        handlers.filter { it in selectedHandlers }.joinToString(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.contentMuted,
                )
            }
            EditDonePillButton(
                isEditing = isEditingHandlers,
                editLabel = stringResource(R.string.finish_walk_edit_times),
                doneLabel = stringResource(R.string.finish_walk_done_times),
                onClick = onToggleEditHandlers,
                enabled = enabled,
            )
        }
        if (isEditingHandlers) {
            SelectableChip(
                items = handlers,
                isSelected = { it in selectedHandlers },
                label = { it },
                onToggle = onHandlerToggle,
                addLabel = stringResource(R.string.new_walk_add_handler),
                onAdd = { showAddHandler = true },
            )
        }
        HorizontalDivider(color = AppTheme.colors.outline)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RowIcon(painterResource(R.drawable.ic_clock))
            Column(modifier = Modifier.weight(1f).padding(start = AppSpacing.md)) {
                Text(
                    text = times.headline(),
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.colors.content,
                )
                times.durationMinutes?.let {
                    Text(
                        text = durationWithDistance(it, distanceMeters),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.contentMuted,
                    )
                }
            }
            EditDonePillButton(
                isEditing = isEditingTimes,
                editLabel = stringResource(R.string.finish_walk_edit_times),
                doneLabel = stringResource(R.string.finish_walk_done_times),
                onClick = onToggleEditTimes,
                enabled = enabled,
            )
        }
        if (isEditingTimes) {
            WhenSection(
                date = times.date,
                maxDate = times.maxDate,
                startTime = times.startTime,
                endTime = times.endTime,
                durationMinutes = times.durationMinutes,
                onDateChange = onDateChange,
                onStartTimeChange = onStartTimeChange,
                onEndTimeChange = onEndTimeChange,
            )
        }
        HorizontalDivider(color = AppTheme.colors.outline)
        WeatherContent(
            weather = weather,
            actions = weatherActions,
            onAllowLocation = onAllowLocation,
            onEdit = weatherActions::toggleWeatherEditing,
        )
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

/** "Today · 07:05 – 07:39", or the date in place of Today for an earlier day. */
@Composable
private fun WalkTimesState.headline(): String {
    val locale = LocalConfiguration.current.locales[0]
    val day = if (date == maxDate) {
        stringResource(R.string.calendar_today)
    } else {
        date.format(DateTimeFormatter.ofPattern("d MMM", locale))
    }
    return stringResource(
        R.string.finish_walk_times,
        day,
        startTime?.format(TimeFormatter).orEmpty(),
        endTime?.format(TimeFormatter).orEmpty(),
    )
}

@Composable
private fun RowIcon(icon: Painter) {
    Icon(
        painter = icon,
        contentDescription = null,
        tint = AppTheme.colors.contentSecondary,
        modifier = Modifier.size(RowIconSize),
    )
}

@Preview(showBackground = true)
@Composable
private fun FinishWalkSummaryCardPreview() {
    val state = LogWalkPreviewData.finishWalk
    WalkLoggerTheme {
        FinishWalkSummaryCard(
            dogName = state.dogName,
            handlers = state.handlers,
            selectedHandlers = state.selectedHandlers,
            isEditingHandlers = false,
            onToggleEditHandlers = {},
            onHandlerToggle = {},
            onAddHandler = {},
            times = state.times,
            distanceMeters = state.distanceMeters,
            isEditingTimes = false,
            onToggleEditTimes = {},
            onDateChange = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            weather = state.form.weather,
            weatherActions = LogWalkPreviewData.formActions,
            onAllowLocation = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
