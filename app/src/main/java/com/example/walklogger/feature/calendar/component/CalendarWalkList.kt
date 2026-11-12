package com.example.walklogger.feature.calendar.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.EquipmentChip
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.calendar.CalendarWalkUiState
import com.example.walklogger.feature.calendar.preview.CalendarPreviewData
import java.time.format.DateTimeFormatter

private val TimeColumnWidth = 56.dp
private val StartTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** The selected day's walks as one compact line each, separated by rules. Tapping a row opens the walk for editing. */
@Composable
internal fun CalendarWalkList(
    walks: List<CalendarWalkUiState>,
    onWalkClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        walks.forEachIndexed { index, walk ->
            if (index > 0) HorizontalDivider(color = AppTheme.colors.outline)
            CalendarWalkRow(walk = walk, onClick = { onWalkClick(walk.id) })
        }
    }
}

@Composable
private fun CalendarWalkRow(walk: CalendarWalkUiState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = walk.startTime.format(StartTimeFormatter),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(TimeColumnWidth),
        )
        val hasNote = walk.note.isNotEmpty()
        Text(
            text = if (hasNote) walk.note else stringResource(R.string.calendar_no_description),
            style = MaterialTheme.typography.bodyLarge,
            color = if (hasNote) Color.Unspecified else AppTheme.colors.contentMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (walk.equipment != null) {
            EquipmentChip(equipment = walk.equipment)
        }
        if (walk.rating != null) {
            Text(text = walk.rating.toString(), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarWalkListPreview() {
    WalkLoggerTheme {
        CalendarWalkList(
            walks = CalendarPreviewData.walks,
            onWalkClick = {},
            modifier = Modifier.padding(horizontal = AppSpacing.lg),
        )
    }
}
