package com.example.walklogger.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AddWalkFab
import com.example.walklogger.core.ui.component.DayWalks
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.component.addWalkClick
import com.example.walklogger.core.ui.component.dayTitle
import com.example.walklogger.core.ui.component.walkSummary
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.calendar.component.MonthGrid
import com.example.walklogger.feature.calendar.component.MonthNavigator
import com.example.walklogger.feature.calendar.preview.CalendarPreviewData
import java.time.LocalDate

@Composable
fun CalendarRoute(
    onAddWalk: (LocalDate) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    onOpenWalk: (Long) -> Unit,
    onGoToDogs: () -> Unit,
    snackbarHostState: SnackbarHostState,
    snackbarHeight: () -> Dp,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onAddWalkClick = addWalkClick(
        date = uiState.selectedDate,
        canAddWalk = uiState.canAddWalk,
        isActiveDogDeleted = uiState.isActiveDogDeleted,
        canStartLiveWalk = uiState.canStartLiveWalk,
        snackbarHostState = snackbarHostState,
        onAddWalk = onAddWalk,
        onLogPastWalk = onLogPastWalk,
        onGoToDogs = onGoToDogs,
    )
    CalendarScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onShowCurrentMonth = viewModel::showCurrentMonth,
        onDaySelect = viewModel::selectDay,
        onAddWalk = onAddWalkClick,
        onWalkClick = onOpenWalk,
        snackbarHeight = snackbarHeight(),
        modifier = modifier,
    )
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShowCurrentMonth: () -> Unit,
    onDaySelect: (LocalDate) -> Unit,
    onAddWalk: () -> Unit,
    onWalkClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHeight: Dp = 0.dp,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.calendar_title),
            actions = {
                DogBadge(name = uiState.activeDogName, isDeleted = uiState.isActiveDogDeleted)
            },
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val viewportHeight = maxHeight
            val density = LocalDensity.current
            var monthHeight by remember { mutableStateOf(0.dp) }
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier.onSizeChanged {
                        monthHeight = with(density) { it.height.toDp() }
                    },
                ) {
                    MonthNavigator(
                        month = uiState.month,
                        walkCount = uiState.monthWalkCount,
                        isCurrentMonth = uiState.isCurrentMonth,
                        canShowNextMonth = uiState.canShowNextMonth,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onShowCurrentMonth = onShowCurrentMonth,
                        modifier = Modifier.padding(horizontal = AppSpacing.sm),
                    )
                    MonthGrid(
                        days = uiState.days,
                        onDaySelect = onDaySelect,
                        modifier = Modifier.padding(AppSpacing.lg),
                    )
                }
                // Fills what the month leaves of the viewport, so the white reaches the bottom on a short day
                // without adding scroll room when the day's walks fit.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = (viewportHeight - monthHeight).coerceAtLeast(0.dp))
                        .background(
                            AppTheme.colors.card,
                            RoundedCornerShape(topStart = AppRadius.xl, topEnd = AppRadius.xl),
                        )
                        .padding(horizontal = AppSpacing.lg)
                        .padding(top = AppSpacing.xxl),
                ) {
                    DayWalks(
                        title = dayTitle(uiState.selectedDate, isToday = uiState.isTodaySelected),
                        summary = if (uiState.walks.isEmpty()) {
                            null
                        } else {
                            walkSummary(
                                uiState.walks.size,
                                averageRating = null,
                                uiState.totalDuration,
                                uiState.totalDistanceMeters,
                            )
                        },
                        walks = uiState.walks,
                        onWalkClick = onWalkClick,
                    )
                    Spacer(Modifier.height(AppSize.buttonHeight + AppSpacing.xxxl))
                }
            }
            AddWalkFab(
                onClick = onAddWalk,
                snackbarHeight = snackbarHeight,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppSpacing.lg),
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
            onDaySelect = {},
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
            onDaySelect = {},
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
            onDaySelect = {},
            onAddWalk = {},
            onWalkClick = {},
        )
    }
}
