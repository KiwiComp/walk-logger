package com.example.walklogger.feature.walkdetail

import com.example.walklogger.feature.logwalk.component.WeatherSummary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.HeaderTextButton
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.FormSection
import com.example.walklogger.feature.logwalk.component.label
import com.example.walklogger.feature.logwalk.component.labelRes
import com.example.walklogger.feature.walkdetail.component.DetailField
import com.example.walklogger.feature.walkdetail.component.DetailPlaceholder
import com.example.walklogger.feature.walkdetail.component.NotedItem
import com.example.walklogger.feature.walkdetail.component.OptionalRatingRow
import com.example.walklogger.feature.walkdetail.component.RatingSummary
import com.example.walklogger.feature.walkdetail.preview.WalkDetailPreviewData
import java.time.LocalDate
import java.time.format.DateTimeFormatter


private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM")
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private const val GridColumns = 2
private const val DateWeight = 3f
private const val TimeWeight = 2f

@Composable
fun WalkDetailRoute(
    walkId: Long,
    onBack: () -> Unit,
    onEdit: (LocalDate, Long) -> Unit,
    onDiscarded: () -> Unit,
    onNotFound: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WalkDetailViewModel = hiltViewModel<WalkDetailViewModel, WalkDetailViewModel.Factory> { it.create(walkId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isDiscarded, uiState.isNotFound) {
        when {
            uiState.isDiscarded -> onDiscarded()
            uiState.isNotFound -> onNotFound()
        }
    }
    WalkDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onEdit = { uiState.date?.let { onEdit(it, walkId) } },
        onDiscard = viewModel::discard,
        modifier = modifier,
    )
}

@Composable
fun WalkDetailScreen(
    uiState: WalkDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.walk_detail_title),
            navigationIcon = {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.back),
                    onClick = onBack,
                )
            },
            actions = {
                HeaderTextButton(
                    text = stringResource(R.string.walk_detail_edit),
                    onClick = onEdit,
                    visible = uiState.canEdit,
                )
                HeaderTextButton(
                    text = stringResource(R.string.walk_detail_discard),
                    onClick = { showDiscardConfirm = true },
                    visible = uiState.canDiscard,
                )
            },
        )
        if (!uiState.isLoading) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                WhenDetails(uiState)
                HorizontalDivider(color = AppTheme.colors.outline)
                WhoDetails(uiState)
                HorizontalDivider(color = AppTheme.colors.outline)
                RatingDetails(uiState)
                HorizontalDivider(color = AppTheme.colors.outline)
                StoolDetails(uiState.stool)
                HorizontalDivider(color = AppTheme.colors.outline)
                OptionalRatingDetails(uiState.optionalRatings)
                HorizontalDivider(color = AppTheme.colors.outline)
                WeatherDetails(uiState)
                HorizontalDivider(color = AppTheme.colors.outline)
                DetailSection(
                    label = stringResource(R.string.log_past_walk_behaviour),
                    hint = uiState.behaviourIssues.size.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.walk_detail_noted_count, it, it) },
                ) {
                    NotedGrid(labels = uiState.behaviourIssues.map { it.label() })
                }
                HorizontalDivider(color = AppTheme.colors.outline)
                DetailSection(
                    label = stringResource(R.string.log_past_walk_medical),
                    hint = uiState.medicalConcerns.size.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.walk_detail_noted_count, it, it) },
                ) {
                    NotedGrid(labels = uiState.medicalConcerns.map { it.label() })
                }
                HorizontalDivider(color = AppTheme.colors.outline)
                DetailSection(label = stringResource(R.string.log_past_walk_notes)) {
                    if (uiState.notes.isBlank()) {
                        DetailPlaceholder(text = stringResource(R.string.walk_detail_not_recorded))
                    } else {
                        Text(
                            text = uiState.notes,
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.colors.content,
                        )
                    }
                }
            }
        }
    }
    if (showDiscardConfirm) {
        DiscardWalkDialog(
            onConfirm = {
                showDiscardConfirm = false
                onDiscard()
            },
            onDismiss = { showDiscardConfirm = false },
        )
    }
}

/** A [FormSection] inset from the screen edges, so the dividers between sections can run edge to edge. */
@Composable
private fun DetailSection(
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    content: @Composable () -> Unit,
) {
    FormSection(
        label = label,
        hint = hint,
        modifier = modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xl),
        content = content,
    )
}

@Composable
private fun WhenDetails(uiState: WalkDetailUiState) {
    DetailSection(label = stringResource(R.string.log_past_walk_when)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            DetailField(
                label = stringResource(R.string.log_past_walk_date),
                value = uiState.date?.format(DateFormatter).orEmpty(),
                modifier = Modifier.weight(DateWeight),
            )
            DetailField(
                label = stringResource(R.string.log_past_walk_start),
                value = uiState.startTime?.format(TimeFormatter).orEmpty(),
                modifier = Modifier.weight(TimeWeight),
            )
            DetailField(
                label = stringResource(R.string.log_past_walk_end),
                value = uiState.endTime?.format(TimeFormatter).orEmpty(),
                modifier = Modifier.weight(TimeWeight),
            )
            DetailField(
                label = stringResource(R.string.walk_detail_length),
                value = uiState.durationMinutes?.let { stringResource(R.string.walk_duration_minutes, it) }.orEmpty(),
                modifier = Modifier.weight(TimeWeight),
            )
        }
    }
}

@Composable
private fun WhoDetails(uiState: WalkDetailUiState) {
    Row(modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xl)) {
        FormSection(label = stringResource(R.string.log_past_walk_handler), modifier = Modifier.weight(1f)) {
            Text(
                text = uiState.handlers.joinToString(),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.content,
            )
        }
        FormSection(label = stringResource(R.string.walk_detail_dog), modifier = Modifier.weight(1f)) {
            Text(
                text = uiState.dogName
                    ?.let { if (uiState.isDogDeleted) stringResource(R.string.dog_name_deleted, it) else it }
                    .orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.content,
            )
        }
        FormSection(label = stringResource(R.string.new_walk_equipment), modifier = Modifier.weight(1f)) {
            Text(
                text = uiState.equipment?.let { stringResource(it.labelRes) }.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.content,
            )
        }
    }
}

@Composable
private fun RatingDetails(uiState: WalkDetailUiState) {
    DetailSection(
        label = stringResource(R.string.log_past_walk_rating),
        hint = stringResource(R.string.log_past_walk_rating_scale),
    ) {
        if (uiState.rating == null) {
            DetailPlaceholder(text = stringResource(R.string.walk_detail_not_recorded))
        } else {
            RatingSummary(rating = uiState.rating)
        }
    }
}

@Composable
private fun StoolDetails(stool: Stool?) {
    DetailSection(label = stringResource(R.string.log_past_walk_stool)) {
        when {
            stool == null -> DetailPlaceholder(text = stringResource(R.string.walk_detail_not_recorded))
            !stool.passed -> DetailPlaceholder(text = stringResource(R.string.walk_detail_stool_none))
            listOf(stool.quantity, stool.texture, stool.colour).all { it == null } ->
                Text(
                    text = stringResource(R.string.stool_passed_yes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.content,
                )
            else -> FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                maxItemsInEachRow = GridColumns,
            ) {
                stool.quantity?.let {
                    DetailField(
                        label = stringResource(R.string.stool_quantity),
                        value = stringResource(it.labelRes),
                        modifier = Modifier.weight(1f),
                    )
                }
                stool.texture?.let {
                    DetailField(
                        label = stringResource(R.string.stool_texture),
                        value = stringResource(it.labelRes),
                        modifier = Modifier.weight(1f),
                    )
                }
                stool.colour?.let { StoolColourField(colour = it, modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun OptionalRatingDetails(ratings: Map<OptionalRating, Int>) {
    DetailSection(
        label = stringResource(R.string.log_past_walk_optional_ratings),
        hint = stringResource(R.string.log_past_walk_optional_ratings_scale).takeIf { ratings.isNotEmpty() },
    ) {
        if (ratings.isEmpty()) {
            DetailPlaceholder(text = stringResource(R.string.walk_detail_not_recorded))
        } else {
            Column {
                OptionalRating.entries.filter { it in ratings }.forEachIndexed { index, aspect ->
                    if (index > 0) HorizontalDivider(color = AppTheme.colors.outline)
                    OptionalRatingRow(
                        label = stringResource(aspect.labelRes),
                        value = ratings.getValue(aspect),
                        modifier = Modifier.padding(vertical = AppSpacing.sm),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDetails(uiState: WalkDetailUiState) {
    DetailSection(label = stringResource(R.string.log_past_walk_weather)) {
        val weather = uiState.weather
        if (weather == null) {
            DetailPlaceholder(text = stringResource(R.string.walk_detail_not_recorded))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                WeatherSummary(weather = weather)
                Text(
                    text = stringResource(R.string.weather_attribution),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.contentMuted,
                )
            }
        }
    }
}

@Composable
private fun NotedGrid(labels: List<String>) {
    if (labels.isEmpty()) {
        DetailPlaceholder(text = stringResource(R.string.walk_detail_nothing_noted))
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            maxItemsInEachRow = GridColumns,
        ) {
            labels.forEach { NotedItem(label = it, modifier = Modifier.weight(1f)) }
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun WalkDetailScreenPreview() {
    WalkLoggerTheme {
        WalkDetailScreen(uiState = WalkDetailPreviewData.complete, onBack = {}, onEdit = {}, onDiscard = {})
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun WalkDetailScreenMinimalPreview() {
    WalkLoggerTheme {
        WalkDetailScreen(uiState = WalkDetailPreviewData.minimal, onBack = {}, onEdit = {}, onDiscard = {})
    }
}
