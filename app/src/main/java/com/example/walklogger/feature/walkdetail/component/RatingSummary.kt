package com.example.walklogger.feature.walkdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val CellSize = 40.dp

/** Read-only echo of the form's rating scale: the chosen cell filled, the value repeated large at the end. */
@Composable
internal fun RatingSummary(rating: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Walk.RATING_RANGE.forEach { value -> RatingCell(value = value, isSelected = value == rating) }
        Spacer(Modifier.weight(1f))
        Text(text = rating.toString(), style = MaterialTheme.typography.headlineLarge, color = Ink)
    }
}

@Composable
private fun RatingCell(value: Int, isSelected: Boolean) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .size(CellSize)
            .clip(shape)
            .background(if (isSelected) Steel else CardSurface)
            .border(1.dp, if (isSelected) Steel else Outline, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) OnNavy else Ink,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingSummaryPreview() {
    WalkLoggerTheme {
        RatingSummary(rating = 5, modifier = Modifier.padding(AppSpacing.lg))
    }
}
