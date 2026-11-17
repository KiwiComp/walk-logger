package com.example.walklogger.feature.calendar.component

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.RatingFace
import com.example.walklogger.core.ui.theme.AppRadius
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
private val CellHeight = 60.dp
private val FaceSize = 22.dp

/** Monday-first month grid; each day is a rounded card with its number and, when rated, its rating face. */
@Composable
internal fun MonthGrid(
    days: List<CalendarDayUiState>,
    onDaySelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { dayOfWeek ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                            .replaceFirstChar { it.titlecase(locale) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.contentSecondary,
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
                    if (day == null) {
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
    val colors = AppTheme.colors
    val background = when {
        day.isSelected -> colors.selection
        !day.isEnabled -> Color.Transparent
        else -> colors.card
    }
    val border = when {
        day.isSelected -> Color.Transparent
        !day.isEnabled -> colors.outline
        day.isToday -> colors.action
        else -> Color.Transparent
    }
    val numberColor = when {
        day.isSelected -> colors.onSelection
        !day.isEnabled -> colors.placeholder
        else -> colors.content
    }
    val locale = LocalConfiguration.current.locales[0]
    val description =
        remember(locale, day.date) {
            day.date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale))
        }
    val walksState = pluralStringResource(R.plurals.walk_count, day.walkCount, day.walkCount)
    val shape = RoundedCornerShape(AppRadius.lg)
    Column(
        modifier = Modifier
            .weight(1f)
            .height(CellHeight)
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
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = numberColor,
        )
        day.rating?.let { RatingFace(rating = it, size = FaceSize) }
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
