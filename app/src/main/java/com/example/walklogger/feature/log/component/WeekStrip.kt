package com.example.walklogger.feature.log.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.log.DayUiState
import com.example.walklogger.feature.log.preview.LogPreviewData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

private val DayCellHeight = 68.dp
private val WalkDotSize = 4.dp

/** Seven-day strip that lives inside the Log header. */
@Composable
internal fun WeekStrip(
    days: List<DayUiState>,
    onDaySelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        days.forEach { day -> DayCell(day = day, onClick = { onDaySelect(day.date) }) }
    }
}

@Composable
private fun RowScope.DayCell(day: DayUiState, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val background = if (day.isSelected) colors.selection else colors.card
    val border = when {
        day.isSelected -> colors.selection
        day.isToday -> colors.action
        else -> colors.outline
    }
    val nameColor = when {
        day.isSelected -> colors.onSelection
        !day.isEnabled -> colors.placeholder
        else -> colors.contentSecondary
    }
    val numberColor = when {
        day.isSelected -> colors.onSelection
        !day.isEnabled -> colors.placeholder
        else -> colors.content
    }
    val walkDot = when {
        !day.hasWalks -> Color.Transparent
        day.isSelected -> colors.actionLight
        else -> colors.action
    }
    val locale = LocalConfiguration.current.locales[0]
    val date =
        remember(locale, day.date) {
            day.date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale))
        }
    val description = if (day.isToday) {
        stringResource(R.string.day_today_description, date)
    } else {
        date
    }
    val walksState =
        stringResource(if (day.hasWalks) R.string.day_has_walks else R.string.day_no_walks)
    val shape = RoundedCornerShape(AppRadius.lg)
    Column(
        modifier = Modifier
            .weight(1f)
            .height(DayCellHeight)
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .selectable(
                selected = day.isSelected,
                enabled = day.isEnabled,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics {
                contentDescription = description
                stateDescription = walksState
            }
            .padding(vertical = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                .replaceFirstChar { it.titlecase(locale) },
            style = MaterialTheme.typography.bodyMedium,
            color = nameColor,
        )
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = numberColor,
        )
        Box(Modifier.size(WalkDotSize).background(walkDot, CircleShape))
    }
}

@Preview
@Composable
private fun WeekStripPreview() {
    WalkLoggerTheme {
        WeekStrip(
            days = LogPreviewData.uiState.week,
            onDaySelect = {},
            modifier = Modifier.background(AppTheme.colors.background).padding(AppSpacing.lg),
        )
    }
}
