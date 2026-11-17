package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Collapsible "Mood & focus" form section; one [OptionalRatingSlider] per [OptionalRating], each clearable on its own. */
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
        label = stringResource(R.string.walk_form_mood_focus),
        hint = stringResource(
            R.string.optional_ratings_rated,
            ratings.size,
            OptionalRating.entries.size,
        ),
        expanded = expanded,
        onToggle = onToggle,
        actionLabel = if (ratings.isNotEmpty()) {
            stringResource(
                R.string.form_section_clear,
            )
        } else {
            null
        },
        onAction = onClear,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            OptionalRating.entries.forEach { aspect ->
                OptionalRatingSlider(
                    label = stringResource(aspect.labelRes),
                    rating = ratings[aspect],
                    onRatingChange = { onRatingChange(aspect, it) },
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                ScaleNote(stringResource(R.string.optional_ratings_least))
                Spacer(Modifier.weight(1f))
                ScaleNote(stringResource(R.string.optional_ratings_most))
            }
        }
    }
}

@Composable
private fun ScaleNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colors.contentMuted,
    )
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
