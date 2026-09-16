package com.example.walklogger.feature.walkdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val SegmentSize = DpSize(width = 12.dp, height = 20.dp)
private val ValueWidth = 20.dp

/** One rated aspect: its [label], a segment per point in [Walk.RATING_RANGE] filled up to [value], and the value. */
@Composable
internal fun OptionalRatingRow(label: String, value: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Walk.RATING_RANGE.forEach { point ->
                Box(Modifier.size(SegmentSize).background(if (point <= value) Steel else Outline))
            }
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            textAlign = TextAlign.End,
            modifier = Modifier.width(ValueWidth),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionalRatingRowPreview() {
    WalkLoggerTheme {
        OptionalRatingRow(label = "Reactivity", value = 6, modifier = Modifier.padding(AppSpacing.lg))
    }
}
