package com.example.walklogger.feature.dog.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.PillButton
import com.example.walklogger.core.ui.component.PillButtonStyle
import com.example.walklogger.core.ui.component.StatusTag
import com.example.walklogger.core.ui.component.StatusTagTone
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.dog.DogListItemUiState
import com.example.walklogger.feature.dog.preview.DogPreviewData
import java.text.NumberFormat

/**
 * The dogs as cards, each with "Active" or a Switch button. Pass [onEditDog] to make the cards
 * open the dog for editing (with a chevron); deleted dogs get a dashed outline and can only be
 * switched to.
 */
@Composable
internal fun DogList(
    dogs: List<DogListItemUiState>,
    onSwitchDog: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onEditDog: ((Long) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        dogs.forEach { dog ->
            DogCard(
                dog = dog,
                onSwitch = { onSwitchDog(dog.id) },
                onEdit = onEditDog?.let { { it(dog.id) } },
            )
        }
    }
}

@Composable
private fun DogCard(dog: DogListItemUiState, onSwitch: () -> Unit, onEdit: (() -> Unit)?) {
    val shape = RoundedCornerShape(AppRadius.xl)
    val isDeleted = onEdit == null
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    val dashColor = AppTheme.colors.dashed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (isDeleted) {
                    Modifier.drawBehind {
                        drawOutline(
                            outline = shape.createOutline(size, layoutDirection, this),
                            color = dashColor,
                            style = Stroke(width = 2f, pathEffect = dash),
                        )
                    }
                } else {
                    Modifier
                        .background(AppTheme.colors.card)
                        .border(1.dp, AppTheme.colors.outline, shape)
                        .clickable(role = Role.Button, onClick = onEdit)
                },
            )
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dog.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDeleted) {
                    AppTheme.colors.contentSecondary
                } else {
                    AppTheme.colors.content
                },
            )
            if (!isDeleted) {
                Text(
                    text = dog.details().ifEmpty { stringResource(R.string.dog_no_details) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.contentMuted,
                )
            }
        }
        if (dog.isActive) {
            StatusTag(text = stringResource(R.string.dog_active), tone = StatusTagTone.Positive)
        } else {
            val description = stringResource(R.string.dog_switch_to, dog.name)
            PillButton(
                text = stringResource(R.string.dog_switch),
                onClick = onSwitch,
                style = PillButtonStyle.Outlined,
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
        if (!isDeleted) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.colors.dashed,
            )
        }
    }
}

/** "Border collie · 3 years · 18 kg", skipping whatever isn't known. */
@Composable
private fun DogListItemUiState.details(): String {
    val locale = LocalConfiguration.current.locales[0]
    val weight = weightKg?.let { kg ->
        val formatted = NumberFormat.getInstance(locale).apply {
            maximumFractionDigits = 1
        }.format(kg)
        stringResource(R.string.dog_weight_kg, formatted)
    }
    return listOfNotNull(
        breed.takeIf { it.isNotBlank() },
        ageYears?.let { pluralStringResource(R.plurals.dog_age_years, it, it) },
        weight,
    ).joinToString(" · ")
}

@Preview(showBackground = true)
@Composable
private fun DogListPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            DogList(dogs = DogPreviewData.profile.dogs, onSwitchDog = {}, onEditDog = {})
            DogList(
                dogs = DogPreviewData.profile.deletedDogs,
                onSwitchDog = {},
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}
