package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val ClearIconSize = 18.dp
private val ClearButtonSize = 32.dp

/**
 * One 1..7 rating as a [StepSlider]. While [rating] is null the row reads "Not rated"; the × beside a
 * value clears it again.
 */
@Composable
internal fun OptionalRatingSlider(
    label: String,
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val range = Walk.RATING_RANGE
    val colors = AppTheme.colors
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = ClearButtonSize),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.content,
                modifier = Modifier.weight(1f),
            )
            if (rating == null) {
                Text(
                    text = stringResource(R.string.optional_rating_not_rated),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.contentMuted,
                )
            } else {
                Text(
                    text = stringResource(R.string.optional_rating_value, rating, range.last),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.action,
                )
                IconButton(
                    onClick = { onRatingChange(null) },
                    modifier = Modifier.size(ClearButtonSize),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.optional_rating_clear, label),
                        tint = colors.contentSecondary,
                        modifier = Modifier.size(ClearIconSize),
                    )
                }
            }
        }
        StepSlider(
            options = range.toList(),
            selected = rating,
            onSelect = onRatingChange,
            label = { it.toString() },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionalRatingSliderPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            OptionalRatingSlider(label = "Reactivity", rating = 2, onRatingChange = {})
            OptionalRatingSlider(label = "Engagement", rating = null, onRatingChange = {})
        }
    }
}
