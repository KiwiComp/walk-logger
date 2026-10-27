package com.example.walklogger.feature.log.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.log.DayUiState
import com.example.walklogger.feature.log.preview.LogPreviewData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle


private val DayCellHeight = 76.dp
private val WalkDotSize = 6.dp
private val TodayDotSize = 12.dp

/** Seven-day strip that lives inside the Log header. */
@Composable
internal fun WeekStrip(
    days: List<DayUiState>,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        days.forEach { day -> DayCell(day = day, onClick = { onDaySelected(day.date) }) }
    }
}

@Composable
private fun RowScope.DayCell(day: DayUiState, onClick: () -> Unit) {
    val background = if (day.isSelected) AppTheme.colors.onHeader else Color.Transparent
    val foreground = when {
        day.isSelected -> AppTheme.colors.header
        !day.isEnabled -> AppTheme.colors.onHeaderMuted
        else -> AppTheme.colors.onHeader
    }
    val locale = LocalConfiguration.current.locales[0]
    val date = remember(locale, day.date) { day.date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale)) }
    val description = if (day.isToday) stringResource(R.string.day_today_description, date) else date
    val walksState = stringResource(if (day.hasWalks) R.string.day_has_walks else R.string.day_no_walks)
    Column(
        modifier = Modifier
            .weight(1f)
            .height(DayCellHeight)
            .background(background)
            .border(1.dp, if (day.isSelected) AppTheme.colors.onHeader else AppTheme.colors.outlineOnHeader)
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
            .padding(vertical = AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (day.isToday) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Box(Modifier.size(TodayDotSize).background(foreground))
            }
        } else {
            SectionLabel(
                text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                color = foreground,
            )
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = foreground,
            )
            Spacer(Modifier.weight(1f))
        }
        Box(
            Modifier
                .size(WalkDotSize)
                .background(if (day.hasWalks) AppTheme.colors.actionLight else Color.Transparent),
        )
    }
}

@Preview
@Composable
private fun WeekStripPreview() {
    WalkLoggerTheme {
        WeekStrip(
            days = LogPreviewData.uiState.week,
            onDaySelected = {},
            modifier = Modifier.background(AppTheme.colors.header).padding(AppSpacing.lg),
        )
    }
}
