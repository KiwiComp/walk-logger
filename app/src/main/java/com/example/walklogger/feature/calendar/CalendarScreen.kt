package com.example.walklogger.feature.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AddWalkButton
import com.example.walklogger.core.ui.component.DayHeading
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.HeaderTextButton
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.component.walkSummary
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.calendar.component.CalendarWalkList
import com.example.walklogger.feature.calendar.component.MonthGrid
import com.example.walklogger.feature.calendar.component.RatingLegend
import com.example.walklogger.feature.calendar.preview.CalendarPreviewData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import kotlinx.coroutines.launch


@Composable
fun CalendarRoute(
    onAddWalk: (LocalDate) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    onOpenWalk: (Long) -> Unit,
    onGoToDogs: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val noDogMessage = stringResource(
        if (uiState.isActiveDogDeleted) R.string.log_deleted_dog_message else R.string.log_no_dog_message,
    )
    val noDogAction = stringResource(R.string.log_no_dog_action)
    CalendarScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onShowCurrentMonth = viewModel::showCurrentMonth,
        onDaySelected = viewModel::selectDay,
        onAddWalk = {
            if (!uiState.canAddWalk) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(message = noDogMessage, actionLabel = noDogAction)
                    if (result == SnackbarResult.ActionPerformed) onGoToDogs()
                }
            } else if (uiState.canStartLiveWalk) {
                onAddWalk(uiState.selectedDate)
            } else {
                onLogPastWalk(uiState.selectedDate)
            }
        },
        onWalkClick = onOpenWalk,
        modifier = modifier,
    )
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShowCurrentMonth: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    onAddWalk: () -> Unit,
    onWalkClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMM yyyy", locale) }
    val dayHeadingFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMMM", locale) }
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = uiState.month.format(monthFormatter).replaceFirstChar { it.titlecase(locale) },
            subtitle = pluralStringResource(R.plurals.walk_count, uiState.monthWalkCount, uiState.monthWalkCount),
            navigationIcon = {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.calendar_previous_month),
                    onClick = onPreviousMonth,
                )
            },
            actions = {
                DogBadge(name = uiState.activeDogName, isDeleted = uiState.isActiveDogDeleted)
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.calendar_next_month),
                    onClick = onNextMonth,
                    enabled = uiState.canShowNextMonth,
                )
            },
        ) {
            Spacer(Modifier.height(AppSpacing.lg))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RatingLegend()
                Spacer(Modifier.weight(1f))
                HeaderTextButton(
                    text = stringResource(R.string.calendar_today),
                    onClick = onShowCurrentMonth,
                    visible = !uiState.isCurrentMonth,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
        ) {
            MonthGrid(days = uiState.days, onDaySelected = onDaySelected)
            Spacer(Modifier.height(AppSpacing.xxl))
            DayHeading(
                title = uiState.selectedDate.format(dayHeadingFormatter),
                summary = walkSummary(uiState.walks.size, uiState.averageRating),
            )
            if (uiState.walks.isNotEmpty()) {
                CalendarWalkList(
                    walks = uiState.walks,
                    onWalkClick = onWalkClick,
                    modifier = Modifier.padding(top = AppSpacing.sm),
                )
            }
            AddWalkButton(
                dayLabel = uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                onClick = onAddWalk,
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CalendarScreenPreview() {
    WalkLoggerTheme {
        CalendarScreen(
            uiState = CalendarPreviewData.uiState,
            onPreviousMonth = {},
            onNextMonth = {},
            onShowCurrentMonth = {},
            onDaySelected = {},
            onAddWalk = {},
            onWalkClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CalendarScreenPastMonthPreview() {
    WalkLoggerTheme {
        CalendarScreen(
            uiState = CalendarPreviewData.uiState.copy(isCurrentMonth = false),
            onPreviousMonth = {},
            onNextMonth = {},
            onShowCurrentMonth = {},
            onDaySelected = {},
            onAddWalk = {},
            onWalkClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CalendarScreenEmptyDayPreview() {
    WalkLoggerTheme {
        CalendarScreen(
            uiState = CalendarPreviewData.uiState.copy(walks = emptyList()),
            onPreviousMonth = {},
            onNextMonth = {},
            onShowCurrentMonth = {},
            onDaySelected = {},
            onAddWalk = {},
            onWalkClick = {},
        )
    }
}
