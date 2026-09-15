package com.example.walklogger.feature.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.format.DateTimeFormatter

/** Width of the time column plus connector; content below the timeline aligns to it. */
internal val TimelineGutter = 84.dp
private val TimelineConnectorWidth = 16.dp
private val TimelineDotSize = 8.dp
private val StartTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** The day's walks, each with its start time and a connector dot leading to a [WalkCard]. */
@Composable
internal fun WalkTimeline(walks: List<WalkUiState>, onWalkClick: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xl)) {
        walks.forEach { walk ->
            Row {
                Column(
                    modifier = Modifier.width(TimelineGutter - TimelineConnectorWidth),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(text = walk.startTime.format(StartTimeFormatter), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.walk_duration_minutes, walk.duration.toMinutes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }
                Box(
                    modifier = Modifier
                        .width(TimelineConnectorWidth)
                        .padding(top = AppSpacing.sm),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(Modifier.size(TimelineDotSize).background(Steel))
                }
                WalkCard(walk = walk, onClick = { onWalkClick(walk.id) }, modifier = Modifier.weight(1f))
            }
        }
    }
    Spacer(Modifier.height(AppSpacing.xl))
}

@Preview(showBackground = true)
@Composable
private fun WalkTimelinePreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            WalkTimeline(walks = LogPreviewData.walks, onWalkClick = {})
        }
    }
}
