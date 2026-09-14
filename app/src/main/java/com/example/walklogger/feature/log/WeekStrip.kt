package com.example.walklogger.feature.log

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.Navy
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.OnNavyMuted
import com.example.walklogger.core.ui.theme.OutlineOnNavy
import com.example.walklogger.core.ui.theme.SteelLight
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DayCellHeight = 76.dp
private val WalkDotSize = 6.dp
private val TodayDotSize = 12.dp
private val DayDescriptionFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM")

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
    val background = if (day.isSelected) OnNavy else Color.Transparent
    val foreground = when {
        day.isSelected -> Navy
        !day.isEnabled -> OnNavyMuted
        else -> OnNavy
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .height(DayCellHeight)
            .background(background)
            .border(1.dp, if (day.isSelected) OnNavy else OutlineOnNavy)
            .selectable(
                selected = day.isSelected,
                enabled = day.isEnabled,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics {
                val date = day.date.format(DayDescriptionFormatter)
                contentDescription = if (day.isToday) "Today, $date" else date
                stateDescription = if (day.hasWalks) "Has walks" else "No walks"
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
                text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
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
                .background(if (day.hasWalks) SteelLight else Color.Transparent),
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
            modifier = Modifier.background(Navy).padding(AppSpacing.lg),
        )
    }
}
