package com.example.walklogger.feature.walkdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.RatingFace
import com.example.walklogger.core.ui.component.durationWithDistance
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.labelRes
import com.example.walklogger.feature.walkdetail.WalkDetailUiState
import com.example.walklogger.feature.walkdetail.preview.WalkDetailPreviewData
import java.time.format.DateTimeFormatter

private val FaceBoxSize = 64.dp
private val FaceSize = 40.dp
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Dark card at the top of walk detail: when, how long, who, and the rating face. */
@Composable
internal fun WalkHeroCard(uiState: WalkDetailUiState, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEE d MMM", locale) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.header, RoundedCornerShape(AppRadius.xl))
            .padding(AppSpacing.xl),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            uiState.date?.let {
                Text(
                    text = it.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onHeaderMuted,
                )
            }
            Text(
                text = listOfNotNull(uiState.startTime, uiState.endTime)
                    .joinToString(" – ") { it.format(TimeFormatter) },
                style = MaterialTheme.typography.headlineLarge,
                color = colors.onHeader,
            )
            uiState.durationMinutes?.let {
                Text(
                    text = durationWithDistance(it, uiState.distanceMeters),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onHeaderMuted,
                )
            }
            Spacer(Modifier.height(AppSpacing.lg))
            WalkedBy(uiState)
            HorizontalDivider(
                color = colors.headerRaised,
                modifier = Modifier.padding(vertical = AppSpacing.md),
            )
            EquipmentUsed(uiState)
        }
        uiState.rating?.let {
            Box(
                modifier = Modifier
                    .size(FaceBoxSize)
                    .background(colors.headerRaised, RoundedCornerShape(AppRadius.lg)),
                contentAlignment = Alignment.Center,
            ) {
                RatingFace(rating = it, size = FaceSize)
            }
        }
    }
}

/** "**Sixten** walked by Kim", with only the dog's name in bold. */
@Composable
private fun WalkedBy(uiState: WalkDetailUiState) {
    val dog = uiState.dogName?.let {
        if (uiState.isDogDeleted) stringResource(R.string.dog_name_deleted, it) else it
    }.orEmpty()
    val text = stringResource(R.string.walk_detail_walked_by, dog, uiState.handlers.joinToString())
    val dogStart = text.indexOf(dog).takeIf { dog.isNotEmpty() && it >= 0 }
    Text(
        text = buildAnnotatedString {
            if (dogStart == null) {
                append(text)
            } else {
                append(text.substring(0, dogStart))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(dog) }
                append(text.substring(dogStart + dog.length))
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        color = AppTheme.colors.onHeader,
    )
}

/** "**Equipment:** Collar", or a muted "Not recorded" in place of the value. */
@Composable
private fun EquipmentUsed(uiState: WalkDetailUiState) {
    val label = stringResource(R.string.new_walk_equipment)
    val value = uiState.equipment?.let { stringResource(it.labelRes) }
        ?: stringResource(R.string.walk_detail_not_recorded)
    val text = stringResource(R.string.label_value, label, value)
    val mutedColor = AppTheme.colors.onHeaderMuted
    Text(
        text = buildAnnotatedString {
            append(text)
            text.indexOf(label).takeIf { it >= 0 }?.let {
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), it, it + label.length)
            }
            if (uiState.equipment == null) {
                text.lastIndexOf(value).takeIf { it >= 0 }?.let {
                    addStyle(SpanStyle(color = mutedColor), it, it + value.length)
                }
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        color = AppTheme.colors.onHeader,
    )
}

@Preview
@Composable
private fun WalkHeroCardPreview() {
    WalkLoggerTheme {
        WalkHeroCard(
            uiState = WalkDetailPreviewData.complete,
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
