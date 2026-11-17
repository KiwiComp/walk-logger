package com.example.walklogger.feature.log

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AddWalkFab
import com.example.walklogger.core.ui.component.DayHeading
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.component.addWalkClick
import com.example.walklogger.core.ui.component.walkSummary
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.log.component.WalkTimeline
import com.example.walklogger.feature.log.component.WeekStrip
import com.example.walklogger.feature.log.preview.LogPreviewData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

@Composable
fun LogRoute(
    onAddWalk: (LocalDate) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    onOpenWalk: (Long) -> Unit,
    onGoToDogs: () -> Unit,
    onOpenSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
    snackbarHeight: () -> Dp,
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = hiltViewModel(),
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
    LogScreen(
        uiState = uiState,
        onDaySelect = viewModel::selectDay,
        onAddWalk = onAddWalkClick,
        onWalkClick = onOpenWalk,
        onOpenSettings = onOpenSettings,
        snackbarHeight = snackbarHeight(),
        modifier = modifier,
    )
}

@Composable
fun LogScreen(
    uiState: LogUiState,
    onDaySelect: (LocalDate) -> Unit,
    onAddWalk: () -> Unit,
    onWalkClick: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHeight: Dp = 0.dp,
) {
    val locale = LocalConfiguration.current.locales[0]
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMM yyyy", locale) }
    val dayFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMM", locale) }
    val weekdayFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEEE d MMM", locale) }
    val weekNumber = uiState.selectedDate.get(WeekFields.of(locale).weekOfWeekBasedYear())
    val isTodaySelected = uiState.week.any { it.isSelected && it.isToday }
    val fabLift by animateDpAsState(snackbarHeight, label = "fabLift")
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.log_title),
            subtitle = stringResource(
                R.string.log_week_label,
                weekNumber,
                uiState.selectedDate.format(monthFormatter),
            ),
            actions = {
                DogBadge(name = uiState.activeDogName, isDeleted = uiState.isActiveDogDeleted)
                HeaderIconButton(
                    icon = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    onClick = onOpenSettings,
                )
            },
        ) {
            Spacer(Modifier.height(AppSpacing.md))
            WeekStrip(days = uiState.week, onDaySelect = onDaySelect)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    AppTheme.colors.card,
                    RoundedCornerShape(topStart = AppRadius.xl, topEnd = AppRadius.xl),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.lg)
                    .padding(top = AppSpacing.xxl),
            ) {
                DayHeading(
                    title = if (isTodaySelected) {
                        stringResource(
                            R.string.day_today_description,
                            uiState.selectedDate.format(dayFormatter),
                        )
                    } else {
                        uiState.selectedDate.format(weekdayFormatter).replaceFirstChar {
                            it.titlecase(locale)
                        }
                    },
                    summary = if (uiState.walks.isEmpty()) {
                        null
                    } else {
                        walkSummary(uiState.walks.size, averageRating = null, uiState.totalDuration)
                    },
                )
                Spacer(Modifier.height(AppSpacing.xxl))
                WalkTimeline(walks = uiState.walks, onWalkClick = onWalkClick)
                Spacer(Modifier.height(AppSize.buttonHeight + AppSpacing.xxxl))
            }
            AddWalkFab(
                onClick = onAddWalk,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppSpacing.lg)
                    .offset { IntOffset(0, -fabLift.roundToPx()) },
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LogScreenPreview() {
    WalkLoggerTheme {
        LogScreen(
            uiState = LogPreviewData.uiState,
            onDaySelect = {},
            onAddWalk = {},
            onWalkClick = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LogScreenEmptyPreview() {
    WalkLoggerTheme {
        LogScreen(
            uiState = LogPreviewData.uiState.copy(walks = emptyList()),
            onDaySelect = {},
            onAddWalk = {},
            onWalkClick = {},
            onOpenSettings = {},
        )
    }
}
