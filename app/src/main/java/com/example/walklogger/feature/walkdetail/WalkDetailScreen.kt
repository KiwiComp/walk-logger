package com.example.walklogger.feature.walkdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.ui.component.AppConfirmDialog
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.PillButton
import com.example.walklogger.core.ui.component.PillButtonStyle
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.component.WalkRouteMap
import com.example.walklogger.core.ui.message.UserMessageEffect
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.StoolSwatch
import com.example.walklogger.feature.logwalk.component.WeatherSummary
import com.example.walklogger.feature.logwalk.component.label
import com.example.walklogger.feature.logwalk.component.labelRes
import com.example.walklogger.feature.walkdetail.component.DetailPlaceholder
import com.example.walklogger.feature.walkdetail.component.WalkHeroCard
import com.example.walklogger.feature.walkdetail.preview.WalkDetailPreviewData
import java.time.LocalDate

private val StoolSwatchSize = 44.dp
private val BarHeight = 4.dp
private val IssuesAndConcernsIconSize = 18.dp

@Suppress("ktlint:compose:parameter-naming")
@Composable
fun WalkDetailRoute(
    walkId: Long,
    onBack: () -> Unit,
    onEdit: (LocalDate, Long) -> Unit,
    onDiscarded: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: WalkDetailViewModel =
        hiltViewModel<WalkDetailViewModel, WalkDetailViewModel.Factory> { it.create(walkId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDiscarded by rememberUpdatedState(onDiscarded)
    val currentOnNotFound by rememberUpdatedState(onNotFound)
    LaunchedEffect(uiState.isDiscarded, uiState.isNotFound) {
        when {
            uiState.isDiscarded -> currentOnDiscarded()
            uiState.isNotFound -> currentOnNotFound()
        }
    }
    UserMessageEffect(
        message = uiState.userMessage,
        snackbarHostState = snackbarHostState,
        onConsume = viewModel::userMessageShown,
    )
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
                if (uiState.canEdit) {
                    PillButton(
                        text = stringResource(R.string.walk_detail_edit),
                        onClick = onEdit,
                        icon = Icons.Outlined.Edit,
                        style = PillButtonStyle.Inverse,
                    )
                }
            },
        )
        if (!uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.lg)
                    .padding(bottom = AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                WalkHeroCard(uiState)
                WalkRouteMap(route = uiState.route)
                WeatherCard(uiState.weather)
                StoolCard(uiState.stool)
                MoodCard(uiState.optionalRatings)
                IssuesAndConcernsCard(uiState)
                NotesCard(uiState.notes)
                if (uiState.canDiscard) {
                    DeleteButton(
                        onClick = { showDiscardConfirm = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
    if (showDiscardConfirm) {
        AppConfirmDialog(
            title = stringResource(R.string.walk_detail_discard_confirm_title),
            message = stringResource(R.string.walk_detail_discard_confirm_message),
            confirmLabel = stringResource(R.string.walk_detail_discard_confirm),
            onConfirm = {
                showDiscardConfirm = false
                onDiscard()
            },
            onDismiss = { showDiscardConfirm = false },
        )
    }
}

/** White card with an optional muted [label] above its content. */
@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.lg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.card)
            .border(1.dp, AppTheme.colors.outline, shape)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        label?.let { Label(it) }
        content()
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colors.contentMuted,
    )
}

@Composable
private fun Value(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, color = AppTheme.colors.content)
}

@Composable
private fun WeatherCard(weather: WalkWeather?) {
    DetailCard(
        label = stringResource(R.string.log_past_walk_weather),
    ) {
        if (weather == null) {
            DetailPlaceholder(stringResource(R.string.walk_detail_not_recorded))
        } else {
            WeatherSummary(weather = weather)
            Text(
                text = stringResource(R.string.weather_attribution),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.contentMuted,
            )
        }
    }
}

@Composable
private fun StoolCard(stool: Stool?) {
    DetailCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            if (stool?.passed == true) {
                stool.colour?.let { StoolSwatch(colour = it, size = StoolSwatchSize) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Label(stringResource(R.string.log_past_walk_stool))
                when {
                    stool == null -> DetailPlaceholder(
                        stringResource(R.string.walk_detail_not_recorded),
                    )

                    !stool.passed -> Value(stringResource(R.string.walk_detail_stool_none))

                    else -> Value(
                        listOfNotNull(
                            stool.quantity?.let { stringResource(it.labelRes) },
                            stool.texture?.let { stringResource(it.labelRes) },
                            stool.colour?.let { stringResource(it.labelRes) },
                        ).ifEmpty { listOf(stringResource(R.string.stool_passed_yes)) }
                            .joinToString(" · "),
                    )
                }
            }
        }
    }
}

@Composable
private fun IssuesAndConcernsCard(uiState: WalkDetailUiState) {
    DetailCard {
        Label(stringResource(R.string.log_past_walk_behaviour))
        IssuesAndConcernsList(uiState.behaviourIssues.map { it.label() })
        HorizontalDivider(
            color = AppTheme.colors.outline,
            modifier = Modifier.padding(vertical = AppSpacing.sm),
        )
        Label(stringResource(R.string.log_past_walk_medical))
        IssuesAndConcernsList(uiState.medicalConcerns.map { it.label() })
    }
}

@Composable
private fun IssuesAndConcernsList(labels: List<String>) {
    if (labels.isEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = AppTheme.colors.action,
                modifier = Modifier.size(IssuesAndConcernsIconSize),
            )
            Text(
                text = stringResource(R.string.walk_detail_nothing_noted),
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.colors.action,
            )
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            labels.forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.colors.onActionLight,
                    modifier = Modifier
                        .background(AppTheme.colors.actionLight, CircleShape)
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                )
            }
        }
    }
}

@Composable
private fun MoodCard(ratings: Map<OptionalRating, Int>) {
    DetailCard(label = stringResource(R.string.walk_form_mood_focus)) {
        if (ratings.isEmpty()) {
            DetailPlaceholder(stringResource(R.string.walk_detail_not_recorded))
            return@DetailCard
        }
        OptionalRating.entries.filter { it in ratings }.forEach { aspect ->
            RatingBar(label = stringResource(aspect.labelRes), value = ratings.getValue(aspect))
        }
        val unrated = OptionalRating.entries.filterNot {
            it in ratings
        }.map { stringResource(it.labelRes) }
        if (unrated.isNotEmpty()) {
            Text(
                text = stringResource(R.string.walk_detail_not_rated, unratedList(unrated)),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.contentMuted,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }
    }
}

/** "Responsiveness, engagement and distraction": the first name as written, the rest in lower case. */
@Composable
private fun unratedList(names: List<String>): String {
    val locale = LocalConfiguration.current.locales[0]
    val words = names.mapIndexed { index, name -> if (index == 0) name else name.lowercase(locale) }
    return if (words.size == 1) {
        words.single()
    } else {
        stringResource(R.string.list_and, words.dropLast(1).joinToString(", "), words.last())
    }
}

@Composable
private fun RatingBar(label: String, value: Int) {
    val range = Walk.RATING_RANGE
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.colors.content,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.optional_rating_value, value, range.last),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.contentMuted,
            )
        }
        val railColor = AppTheme.colors.rail
        val fillColor = AppTheme.colors.action
        val fraction = (value - range.first).toFloat() / (range.last - range.first)
        Canvas(Modifier.fillMaxWidth().height(BarHeight)) {
            val y = size.height / 2
            val stroke = BarHeight.toPx()
            // Round caps reach half a stroke past each end, so the line is inset to stay inside the text edge.
            val start = stroke / 2
            val end = size.width - stroke / 2
            drawLine(railColor, Offset(start, y), Offset(end, y), stroke, StrokeCap.Round)
            drawLine(
                fillColor,
                Offset(start, y),
                Offset(start + (end - start) * fraction, y),
                stroke,
                StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    if (notes.isNotBlank()) {
        DetailCard(label = stringResource(R.string.log_past_walk_notes)) {
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.content,
            )
        }
        return
    }
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    val dashColor = AppTheme.colors.dashed
    val shape = RoundedCornerShape(AppRadius.lg)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = dashColor,
                    style = Stroke(width = 2f, pathEffect = dash),
                )
            }
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Label(stringResource(R.string.walk_detail_not_recorded))
        Text(
            text = stringResource(R.string.log_past_walk_notes),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.content,
        )
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = null,
            tint = AppTheme.colors.danger,
        )
        Text(
            text = stringResource(R.string.walk_detail_delete),
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.danger,
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun WalkDetailScreenPreview() {
    WalkLoggerTheme {
        WalkDetailScreen(uiState = WalkDetailPreviewData.complete, onBack = {
        }, onEdit = {}, onDiscard = {})
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun WalkDetailScreenMinimalPreview() {
    WalkLoggerTheme {
        WalkDetailScreen(uiState = WalkDetailPreviewData.minimal, onBack = {
        }, onEdit = {}, onDiscard = {})
    }
}
