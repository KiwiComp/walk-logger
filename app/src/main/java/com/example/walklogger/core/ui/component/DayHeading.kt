package com.example.walklogger.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private const val METERS_PER_KM = 1_000.0

/** Day title with a muted summary below it, which has its own line as it grows too long to fit beside. */
@Composable
fun DayHeading(title: String, summary: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        if (summary != null) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.contentMuted,
            )
        }
    }
}

/** "Today, 17 Nov" for today, otherwise "Tuesday 3 Nov". */
@Composable
fun dayTitle(date: LocalDate, isToday: Boolean): String {
    val locale = LocalConfiguration.current.locales[0]
    return if (isToday) {
        stringResource(
            R.string.day_today_description,
            date.format(DateTimeFormatter.ofPattern("d MMM", locale)),
        )
    } else {
        date.format(DateTimeFormatter.ofPattern("EEEE d MMM", locale))
            .replaceFirstChar { it.titlecase(locale) }
    }
}

/** "2 walks · 1 h 5 min · 3.2 km · avg 5.0"; the duration, distance and average are left out when there is none. */
@Composable
fun walkSummary(
    walkCount: Int,
    averageRating: Double?,
    totalDuration: Duration,
    totalDistanceMeters: Double? = null,
): String {
    val count = pluralStringResource(R.plurals.walk_count, walkCount, walkCount)
    val duration = totalDuration.takeIf { it > Duration.ZERO }?.let {
        " " + stringResource(R.string.summary_duration, formatDuration(it))
    }.orEmpty()
    val distance = totalDistanceMeters?.let {
        " " + stringResource(R.string.summary_distance, formatDistance(it))
    }.orEmpty()
    val average = averageRating?.let {
        " " + stringResource(R.string.summary_average, it)
    }.orEmpty()
    return count + duration + distance + average
}

/** "45 min", or "1 h 5 min" from an hour up. */
@Composable
internal fun formatDuration(duration: Duration): String =
    duration.toComponents { hours, minutes, _, _ ->
        if (hours > 0) {
            stringResource(R.string.summary_duration_hours_minutes, hours, minutes)
        } else {
            stringResource(R.string.walk_duration_minutes, minutes)
        }
    }

/** "3.2 km", one decimal in the user's locale. */
@Composable
internal fun formatDistance(meters: Double): String =
    stringResource(R.string.walk_distance_km, meters / METERS_PER_KM)

/** "38 min · 3.2 km", or just "38 min" without a distance. */
@Composable
internal fun durationWithDistance(durationMinutes: Int, distanceMeters: Double?): String {
    val duration = stringResource(R.string.walk_duration_minutes, durationMinutes)
    return distanceMeters?.let {
        stringResource(R.string.walk_duration_with_distance, duration, formatDistance(it))
    } ?: duration
}

@Preview(showBackground = true)
@Composable
private fun DayHeadingPreview() {
    WalkLoggerTheme {
        DayHeading(
            title = "Saturday 12 Sep",
            summary = walkSummary(
                walkCount = 2,
                averageRating = 5.0,
                totalDuration = 1.hours + 5.minutes,
                totalDistanceMeters = 3_240.0,
            ),
        )
    }
}
