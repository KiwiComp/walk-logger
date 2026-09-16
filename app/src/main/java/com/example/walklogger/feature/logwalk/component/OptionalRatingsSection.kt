package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Collapsible "Optional ratings" form section; one [RatingScale] per [OptionalRating], each clearable on its own. */
@Composable
internal fun OptionalRatingsSection(
    ratings: Map<OptionalRating, Int>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit,
    onRatingChange: (OptionalRating, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FormSection(
        label = stringResource(R.string.log_past_walk_optional_ratings),
        hint = stringResource(R.string.log_past_walk_optional_ratings_scale),
        expanded = expanded,
        onToggle = onToggle,
        actionLabel = if (ratings.isNotEmpty()) stringResource(R.string.form_section_clear) else null,
        onAction = onClear,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            OptionalRating.entries.forEach { aspect ->
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(
                        text = stringResource(aspect.labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                    )
                    RatingScale(
                        rating = ratings[aspect],
                        onRatingChange = { onRatingChange(aspect, it) },
                        onDeselect = { onRatingChange(aspect, null) },
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionalRatingsSectionPreview() {
    WalkLoggerTheme {
        OptionalRatingsSection(
            ratings = mapOf(OptionalRating.REACTIVITY to 2, OptionalRating.ENERGY_LEVEL to 6),
            expanded = true,
            onToggle = {},
            onClear = {},
            onRatingChange = { _, _ -> },
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
