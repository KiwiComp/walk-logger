package com.example.walklogger.feature.log.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.component.formatDuration
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.log.WalkUiState
import com.example.walklogger.feature.log.preview.LogPreviewData
import java.time.format.DateTimeFormatter

private val TimeColumnWidth = 72.dp
private val ConnectorWidth = 32.dp
private val DotSize = 12.dp
private val RailWidth = 2.dp
private val RowGap = AppSpacing.lg
private val StartTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** The day's walks, each with its start time and a dot on a rail leading to a [WalkCard]. */
@Composable
internal fun WalkTimeline(
    walks: List<WalkUiState>,
    onWalkClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(RowGap)) {
        walks.forEachIndexed { index, walk ->
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(
                    modifier = Modifier.width(TimeColumnWidth),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(
                        text = walk.startTime.format(StartTimeFormatter),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatDuration(walk.duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.contentMuted,
                    )
                }
                TimelineConnector(isLast = index == walks.lastIndex)
                WalkCard(
                    walk = walk,
                    onClick = { onWalkClick(walk.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Dot level with the start time; the rail runs on past this row's bottom to the next row's dot. */
@Composable
private fun TimelineConnector(isLast: Boolean) {
    val railColor = AppTheme.colors.rail
    Box(
        modifier = Modifier
            .width(ConnectorWidth)
            .fillMaxHeight()
            .padding(top = AppSpacing.xs)
            .drawBehind {
                if (!isLast) {
                    val x = size.width / 2
                    val dotCenter = DotSize.toPx() / 2
                    drawLine(
                        color = railColor,
                        start = Offset(x, dotCenter),
                        end = Offset(x, size.height + RowGap.toPx() + dotCenter),
                        strokeWidth = RailWidth.toPx(),
                    )
                }
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(Modifier.size(DotSize).background(AppTheme.colors.action, CircleShape))
    }
}

@Preview(showBackground = true)
@Composable
private fun WalkTimelinePreview() {
    WalkLoggerTheme {
        WalkTimeline(
            walks = LogPreviewData.walks,
            onWalkClick = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
