package com.example.walklogger.feature.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AddWalkButton
import com.example.walklogger.core.ui.component.DayHeading
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.component.addWalkClick
import com.example.walklogger.core.ui.component.walkSummary
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.log.component.WalkTimeline
import com.example.walklogger.feature.log.component.WeekStrip
import com.example.walklogger.feature.log.preview.LogPreviewData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun LogRoute(
    onAddWalk: (LocalDate) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    onOpenWalk: (Long) -> Unit,
    onGoToDogs: () -> Unit,
    snackbarHostState: SnackbarHostState,
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
        modifier = modifier,
    )
}

@Composable
fun LogScreen(
    uiState: LogUiState,
    onDaySelect: (LocalDate) -> Unit,
    onAddWalk: () -> Unit,
    onWalkClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val dayHeadingFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEEE d MMM", locale) }
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.log_title),
            actions = {
                DogBadge(name = uiState.activeDogName, isDeleted = uiState.isActiveDogDeleted)
            },
        ) {
            Spacer(Modifier.height(AppSpacing.xl))
            WeekStrip(days = uiState.week, onDaySelect = onDaySelect)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
        ) {
            DayHeading(
                title = uiState.selectedDate.format(dayHeadingFormatter).replaceFirstChar {
                    it.titlecase(locale)
                },
                summary = if (uiState.walks.isEmpty()) {
                    null
                } else {
                    walkSummary(
                        uiState.walks.size,
                        uiState.averageRating,
                    )
                },
            )
            Spacer(Modifier.height(AppSpacing.lg))
            if (uiState.walks.isNotEmpty()) {
                WalkTimeline(walks = uiState.walks, onWalkClick = onWalkClick)
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
private fun LogScreenPreview() {
    WalkLoggerTheme {
        LogScreen(
            uiState = LogPreviewData.uiState,
            onDaySelect = {},
            onAddWalk = {},
            onWalkClick = {},
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
        )
    }
}
