package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme

/** Every card reserves this many note lines so the timeline stays even; longer notes are cut with an ellipsis. */
private const val NOTE_LINES = 2

/** One walk in the day's timeline: rating face, equipment and note. Tapping it opens the walk. */
@Composable
fun WalkCard(walk: TimelineWalk, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppRadius.lg)
    Row(
        modifier = modifier
            .clip(shape)
            .background(AppTheme.colors.cardSubtle)
            .border(1.dp, AppTheme.colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                if (walk.rating != null) {
                    RatingFace(
                        rating = walk.rating,
                        modifier = Modifier.padding(end = AppSpacing.xs),
                    )
                }
                if (walk.equipment != null) {
                    EquipmentChip(equipment = walk.equipment)
                }
            }
            val hasNote = walk.note.isNotEmpty()
            val noteStyle = MaterialTheme.typography.bodyLarge
            Text(
                text = if (hasNote) walk.note else stringResource(R.string.calendar_no_description),
                style = if (hasNote) noteStyle else noteStyle.copy(fontStyle = FontStyle.Italic),
                color = if (hasNote) {
                    AppTheme.colors.contentSecondary
                } else {
                    AppTheme.colors.placeholder
                },
                minLines = NOTE_LINES,
                maxLines = NOTE_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.dashed,
            modifier = Modifier.padding(start = AppSpacing.sm),
        )
    }
}
