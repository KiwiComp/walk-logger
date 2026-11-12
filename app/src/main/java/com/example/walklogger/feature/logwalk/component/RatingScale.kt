package com.example.walklogger.feature.logwalk.component

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
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * One square cell per value in [Walk.RATING_RANGE]; [rating] is null until the user picks.
 * Pass [onDeselect] to let a tap on the selected cell clear the rating again.
 */
@Composable
internal fun RatingScale(
    rating: Int?,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onDeselect: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Walk.RATING_RANGE.forEach { value ->
            val isSelected = value == rating
            RatingCell(
                value = value,
                isSelected = isSelected,
                onClick = {
                    if (isSelected &&
                        onDeselect != null
                    ) {
                        onDeselect()
                    } else {
                        onRatingChange(value)
                    }
                },
            )
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
            .background(if (isSelected) AppTheme.colors.action else AppTheme.colors.card)
            .border(
                1.dp,
                if (isSelected) AppTheme.colors.action else AppTheme.colors.outline,
                shape,
            )
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) AppTheme.colors.onHeader else AppTheme.colors.content,
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
