package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.component.RatingFace
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val SelectedBorderWidth = 2.dp

/** One face per value in [Walk.RATING_RANGE], worst to best; [rating] is null until the user picks. */
@Composable
internal fun RatingScale(
    rating: Int?,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Walk.RATING_RANGE.forEach { value ->
                FaceCell(value = value, isSelected = value == rating, onClick = {
                    onRatingChange(value)
                })
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            ScaleEnd(stringResource(R.string.rating_rough))
            Box(Modifier.weight(1f))
            ScaleEnd(stringResource(R.string.rating_great))
        }
    }
}

@Composable
private fun RowScope.FaceCell(value: Int, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.md)
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(shape)
            .background(if (isSelected) AppTheme.colors.actionLight else Color.Transparent)
            .border(
                SelectedBorderWidth,
                if (isSelected) AppTheme.colors.action else Color.Transparent,
                shape,
            )
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(AppSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        RatingFace(rating = value)
    }
}

@Composable
private fun ScaleEnd(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colors.contentMuted,
    )
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
