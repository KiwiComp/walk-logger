package com.example.walklogger.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.example.walklogger.core.ui.theme.Navy
import com.example.walklogger.core.ui.theme.OnNavyMuted
import com.example.walklogger.core.ui.theme.RatingTints
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val SwatchSize = 14.dp

/** Explains the day tints inside the Calendar header: one swatch per rating. */
@Composable
internal fun RatingLegend(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.calendar_legend_rating),
            style = MaterialTheme.typography.bodySmall,
            color = OnNavyMuted,
        )
        Spacer(Modifier.width(AppSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            RatingTints.forEach { tint -> Box(Modifier.size(SwatchSize).background(tint)) }
        }
        Spacer(Modifier.width(AppSpacing.sm))
        Text(
            text = stringResource(R.string.calendar_legend_range),
            style = MaterialTheme.typography.bodySmall,
            color = OnNavyMuted,
        )
    }
}

@Preview
@Composable
private fun RatingLegendPreview() {
    WalkLoggerTheme {
        RatingLegend(modifier = Modifier.background(Navy).padding(AppSpacing.lg))
    }
}
