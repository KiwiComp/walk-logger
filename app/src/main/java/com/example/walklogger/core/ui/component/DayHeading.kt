package com.example.walklogger.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Day title with a muted summary hanging off its baseline. */
@Composable
fun DayHeading(title: String, summary: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        if (summary != null) {
            Spacer(Modifier.width(AppSpacing.md))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.contentMuted,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
    }
}

/** "2 walks · avg 5.0"; the average is left out when there is none. */
@Composable
fun walkSummary(walkCount: Int, averageRating: Double?): String {
    val count = pluralStringResource(R.plurals.walk_count, walkCount, walkCount)
    val average = averageRating?.let {
        " " + stringResource(R.string.summary_average, it)
    }.orEmpty()
    return count + average
}

@Preview(showBackground = true)
@Composable
private fun DayHeadingPreview() {
    WalkLoggerTheme {
        DayHeading(
            title = "Saturday 12 Sep",
            summary = walkSummary(walkCount = 2, averageRating = 5.0),
        )
    }
}
