package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

private val TimeColumnWidth = 72.dp
private val ConnectorWidth = 32.dp
private val DotSize = 12.dp
private val RailWidth = 2.dp
private val RowGap = AppSpacing.lg
private val StartTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** The day's walks, each with its start time and a dot on a rail leading to a [WalkCard]. */
@Composable
fun WalkTimeline(
    walks: List<TimelineWalk>,
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
                    walk.distanceMeters?.let {
                        Text(
                            text = formatDistance(it),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.contentMuted,
                        )
                    }
                    if (walk.hasPassedStool == true) {
                        val description = stringResource(R.string.walk_stool_passed_description)
                        Text(
                            text = stringResource(R.string.walk_stool_passed_emoji),
                            modifier = Modifier.semantics { contentDescription = description },
                        )
                    }
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

/** The day's [title] with its [summary], then its walks; shared by the Log and Calendar sheets. */
@Composable
fun DayWalks(
    title: String,
    summary: String?,
    walks: List<TimelineWalk>,
    onWalkClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DayHeading(title = title, summary = summary)
        Spacer(Modifier.height(AppSpacing.xxl))
        WalkTimeline(walks = walks, onWalkClick = onWalkClick)
    }
}

private val PreviewWalks = listOf(
    TimelineWalk(
        id = 1,
        startTime = LocalTime.of(7, 5),
        duration = 38.minutes,
        distanceMeters = 3_240.0,
        equipment = Equipment.HARNESS,
        rating = 6,
        note = "Long morning loop, calm the whole way.",
        hasPassedStool = null,
    ),
    TimelineWalk(
        id = 2,
        startTime = LocalTime.of(11, 30),
        duration = 177.minutes,
        distanceMeters = null,
        equipment = Equipment.COLLAR,
        rating = 4,
        note = "Quick errand walk, distracted by the market.",
        hasPassedStool = true,
    ),
)

@Preview(showBackground = true)
@Composable
private fun WalkTimelinePreview() {
    WalkLoggerTheme {
        WalkTimeline(
            walks = PreviewWalks,
            onWalkClick = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WalkCardPreview() {
    WalkLoggerTheme {
        WalkCard(walk = PreviewWalks[1], onClick = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}
