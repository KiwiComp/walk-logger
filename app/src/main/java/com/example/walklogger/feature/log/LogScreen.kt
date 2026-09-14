package com.example.walklogger.feature.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun LogRoute(modifier: Modifier = Modifier, viewModel: LogViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LogScreen(
        uiState = uiState,
        onDaySelected = viewModel::selectDay,
        onAddWalk = viewModel::addWalk,
        modifier = modifier,
    )
}

@Composable
fun LogScreen(
    uiState: LogUiState,
    onDaySelected: (LocalDate) -> Unit,
    onAddWalk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val dayHeadingFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEEE d MMM", locale) }
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.log_title),
            actions = { DogBadge(name = "Sixten") },
        ) {
            Spacer(Modifier.height(AppSpacing.xl))
            WeekStrip(days = uiState.week, onDaySelected = onDaySelected)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
        ) {
            DayHeading(
                title = uiState.selectedDate.format(dayHeadingFormatter).replaceFirstChar { it.titlecase(locale) },
                summary = uiState.summary(),
            )
            Spacer(Modifier.height(AppSpacing.lg))
            if (uiState.walks.isNotEmpty()) {
                WalkTimeline(walks = uiState.walks)
            }
            AddWalkButton(
                dayLabel = uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                onClick = onAddWalk,
                modifier = Modifier.padding(top = AppSpacing.lg)
            )
        }
    }
}

@Composable
private fun LogUiState.summary(): String? {
    if (walks.isEmpty()) return null
    val count = pluralStringResource(R.plurals.walk_count, walks.size, walks.size)
    val average = averageRating?.let { " " + stringResource(R.string.summary_average, it) }.orEmpty()
    return count + average
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LogScreenPreview() {
    WalkLoggerTheme {
        LogScreen(uiState = LogPreviewData.uiState, onDaySelected = {}, onAddWalk = {})
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LogScreenEmptyPreview() {
    WalkLoggerTheme {
        LogScreen(uiState = LogPreviewData.uiState.copy(walks = emptyList()), onDaySelected = {}, onAddWalk = {})
    }
}
