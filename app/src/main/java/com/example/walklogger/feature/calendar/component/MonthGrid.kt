package com.example.walklogger.feature.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
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
import com.example.walklogger.feature.calendar.CalendarDayUiState
import com.example.walklogger.feature.calendar.preview.CalendarPreviewData
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

private const val DAYS_PER_WEEK = 7
private val SelectedBorderWidth = 2.dp

/** Ratings from here up sit on the dark blues, where [AppTheme.colors.content] would no longer be readable. */
private const val FIRST_DARK_RATING = 5

/** Monday-first month grid; each cell is tinted by its day's rating and shows the walk count. */
@Composable
internal fun MonthGrid(
    days: List<CalendarDayUiState>,
    onDaySelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.xs)) {
            DayOfWeek.entries.forEach { dayOfWeek ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    SectionLabel(
                        text = dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                        color = AppTheme.colors.contentMuted,
                    )
                }
            }
        }
        val leadingBlanks = days.first().date.dayOfWeek.value - DayOfWeek.MONDAY.value
        val cells: List<CalendarDayUiState?> = List(leadingBlanks) { null } + days
        cells.chunked(DAYS_PER_WEEK).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                week.forEach { day ->
                    if (day ==
                        null
                    ) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        DayCell(day, onClick = { onDaySelect(day.date) })
                    }
                }
                repeat(DAYS_PER_WEEK - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(day: CalendarDayUiState, onClick: () -> Unit) {
    val background = day.rating?.let { AppTheme.colors.ratingTints[it - 1] } ?: AppTheme.colors.chip
    val foreground = when {
        !day.isEnabled -> AppTheme.colors.contentMuted
        day.rating != null && day.rating >= FIRST_DARK_RATING -> AppTheme.colors.onHeader
        else -> AppTheme.colors.content
    }
    val locale = LocalConfiguration.current.locales[0]
    val description =
        remember(locale, day.date) {
            day.date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale))
        }
    val walksState = pluralStringResource(R.plurals.walk_count, day.walkCount, day.walkCount)
    Column(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .background(background)
            .border(
                SelectedBorderWidth,
                if (day.isSelected) AppTheme.colors.selection else Color.Transparent,
            )
            .selectable(
                selected = day.isSelected,
                enabled = day.isEnabled,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics {
                contentDescription = description
                stateDescription = walksState
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = foreground,
        )
        if (day.walkCount > 0) {
            Text(
                text = day.walkCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = foreground,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthGridPreview() {
    WalkLoggerTheme {
        MonthGrid(
            days = CalendarPreviewData.uiState.days,
            onDaySelect = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
