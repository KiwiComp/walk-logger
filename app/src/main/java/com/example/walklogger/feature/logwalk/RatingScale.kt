package com.example.walklogger.feature.logwalk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.CardSurface
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** One square cell per value in [Walk.RATING_RANGE]; [rating] is null until the user picks. */
@Composable
internal fun RatingScale(rating: Int?, onRatingChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Walk.RATING_RANGE.forEach { value ->
            RatingCell(value = value, isSelected = value == rating, onClick = { onRatingChange(value) })
        }
    }
}

@Composable
private fun RowScope.RatingCell(value: Int, isSelected: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(shape)
            .background(if (isSelected) Steel else CardSurface)
            .border(1.dp, if (isSelected) Steel else Outline, shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) OnNavy else Ink,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingScalePreview() {
    WalkLoggerTheme {
        RatingScale(rating = 5, onRatingChange = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingScaleUnsetPreview() {
    WalkLoggerTheme {
        RatingScale(rating = null, onRatingChange = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}
