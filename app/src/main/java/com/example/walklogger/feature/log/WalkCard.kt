package com.example.walklogger.feature.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.CardSurface
import com.example.walklogger.core.ui.theme.ChipSurface
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val RatingBarWidth = 9.dp
private val RatingBarHeight = 16.dp
/** Every card reserves this many note lines so the timeline stays even; longer notes are cut with an ellipsis. */
private const val NoteLines = 2

/** One walk in the day's timeline: equipment chip, rating and note. Tapping it opens the walk for editing. */
@Composable
internal fun WalkCard(walk: WalkUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardSurface)
            .border(1.dp, Outline)
            .clickable(onClick = onClick)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (walk.equipment != null) {
                Text(
                    text = walk.equipment.label(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .background(ChipSurface)
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                )
            }
            Spacer(Modifier.weight(1f))
            if (walk.rating != null) {
                RatingBar(rating = walk.rating)
            }
        }
        Text(
            text = walk.note,
            style = MaterialTheme.typography.bodyLarge,
            minLines = NoteLines,
            maxLines = NoteLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Equipment.label() = stringResource(
    when (this) {
        Equipment.COLLAR -> R.string.equipment_collar
        Equipment.HARNESS -> R.string.equipment_harness
    },
)

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
        WalkCard(walk = LogPreviewData.walk, onClick = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}
