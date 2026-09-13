package com.example.walklogger.feature.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.CardSurface
import com.example.walklogger.core.ui.theme.ChipSurface
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val RatingBarWidth = 9.dp
private val RatingBarHeight = 16.dp

/** One walk in the day's timeline: equipment chip, rating and note. */
@Composable
internal fun WalkCard(walk: SampleWalk, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardSurface)
            .border(1.dp, Outline)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = walk.equipment,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .background(ChipSurface)
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            )
            Spacer(Modifier.weight(1f))
            RatingBar(rating = walk.rating)
        }
        Text(text = walk.note, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun RatingBar(rating: Int, max: Int = 7) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        repeat(max) { index ->
            Box(
                Modifier
                    .size(width = RatingBarWidth, height = RatingBarHeight)
                    .background(if (index < rating) Steel else Outline),
            )
        }
        Spacer(Modifier.width(AppSpacing.sm))
        Text(text = rating.toString(), style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun WalkCardPreview() {
    WalkLoggerTheme {
        WalkCard(
            walk = SampleWalk("07:05", "38 min", "Harness", 6, "Long morning loop, calm the whole way."),
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
