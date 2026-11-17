package com.example.walklogger.feature.logwalk.activewalk

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppConfirmDialog
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.message.UserMessageEffect
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.FormSection
import com.example.walklogger.feature.logwalk.component.labelRes
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData
import java.time.Duration
import java.time.format.DateTimeFormatter

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private const val NOTE_MIN_LINES = 2
private val LiveDotSize = 8.dp
private val StopIconSize = 12.dp

/** The ways off this screen are ending or discarding the walk; system back asks to discard. */
@Suppress("ktlint:compose:parameter-naming")
@Composable
fun ActiveWalkRoute(
    walkId: Long,
    onWalkEnded: (walkId: Long) -> Unit,
    onWalkDiscarded: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: ActiveWalkViewModel =
        hiltViewModel<ActiveWalkViewModel, ActiveWalkViewModel.Factory> { it.create(walkId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnWalkEnded by rememberUpdatedState(onWalkEnded)
    val currentOnWalkDiscarded by rememberUpdatedState(onWalkDiscarded)
    val currentOnNotFound by rememberUpdatedState(onNotFound)
    LaunchedEffect(uiState.isEnded, uiState.isDiscarded, uiState.isNotFound) {
        when {
            uiState.isEnded -> currentOnWalkEnded(walkId)
            uiState.isDiscarded -> currentOnWalkDiscarded()
            uiState.isNotFound -> currentOnNotFound()
        }
    }
    UserMessageEffect(
        message = uiState.userMessage,
        snackbarHostState = snackbarHostState,
        onConsume = viewModel::userMessageShown,
    )
    ActiveWalkScreen(
        uiState = uiState,
        onNoteChange = viewModel::setNote,
        onStoolPassedChange = viewModel::setStoolPassed,
        onStoolQuantitySelect = viewModel::selectStoolQuantity,
        onStoolTextureSelect = viewModel::selectStoolTexture,
        onStoolColourSelect = viewModel::selectStoolColour,
        onStoolFold = viewModel::foldStool,
        onStoolExpand = viewModel::expandStool,
        onEndWalk = viewModel::endWalk,
        onDiscardWalk = viewModel::discardWalk,
        modifier = modifier,
    )
}

@Composable
fun ActiveWalkScreen(
    uiState: ActiveWalkUiState,
    onNoteChange: (String) -> Unit,
    onStoolPassedChange: (Boolean) -> Unit,
    onStoolQuantitySelect: (StoolQuantity) -> Unit,
    onStoolTextureSelect: (StoolTexture) -> Unit,
    onStoolColourSelect: (StoolColour?) -> Unit,
    onStoolFold: () -> Unit,
    onStoolExpand: () -> Unit,
    onEndWalk: () -> Unit,
    onDiscardWalk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }
    BackHandler { showDiscardConfirm = uiState.canDiscard }
    Column(modifier = modifier.fillMaxSize().imePadding()) {
        WalkLoggerHeader(title = stringResource(R.string.active_walk_title))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg)
                .padding(top = AppSpacing.md, bottom = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            TimerCard(uiState)
            FormSection(label = stringResource(R.string.active_walk_quick_note)) {
                AppTextField(
                    value = uiState.note,
                    onValueChange = onNoteChange,
                    placeholder = stringResource(R.string.active_walk_quick_note_placeholder),
                    singleLine = false,
                    minLines = NOTE_MIN_LINES,
                )
            }
            ActiveStoolSection(
                stool = uiState.stool,
                expanded = uiState.isStoolExpanded,
                onPassedChange = onStoolPassedChange,
                onQuantitySelect = onStoolQuantitySelect,
                onTextureSelect = onStoolTextureSelect,
                onColourSelect = onStoolColourSelect,
                onFold = onStoolFold,
                onExpand = onStoolExpand,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.card)
                .navigationBarsPadding(),
        ) {
            HorizontalDivider(color = AppTheme.colors.outline)
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppButton(
                    text = stringResource(R.string.active_walk_end),
                    onClick = onEndWalk,
                    enabled = uiState.canEnd || uiState.isEnding,
                    loading = uiState.isEnding,
                    leadingIcon = {
                        Box(
                            Modifier
                                .size(StopIconSize)
                                .background(
                                    LocalContentColor.current,
                                    RoundedCornerShape(AppRadius.xs),
                                ),
                        )
                    },
                )
                if (uiState.canDiscard) {
                    DiscardButton(onClick = { showDiscardConfirm = true })
                }
            }
        }
    }
    if (showDiscardConfirm) {
        AppConfirmDialog(
            title = stringResource(R.string.active_walk_discard_confirm_title),
            message = stringResource(R.string.active_walk_discard_confirm_message),
            confirmLabel = stringResource(R.string.active_walk_discard_confirm),
            onConfirm = {
                showDiscardConfirm = false
                onDiscardWalk()
            },
            onDismiss = { showDiscardConfirm = false },
        )
    }
}

/** Dark card with who is out, the running time and how the walk started. */
@Composable
private fun TimerCard(uiState: ActiveWalkUiState) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.header, RoundedCornerShape(AppRadius.xl))
            .padding(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(Modifier.size(LiveDotSize).background(colors.actionLight, CircleShape))
            Text(
                text = uiState.dogName?.let { stringResource(R.string.active_walk_out_with, it) }
                    ?: stringResource(R.string.active_walk_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onHeaderMuted,
            )
        }
        Text(
            text = uiState.elapsed.toClockText(),
            style = MaterialTheme.typography.displayLarge,
            color = colors.onHeader,
        )
        Text(
            text = uiState.startedSummary(),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onHeaderMuted,
        )
    }
}

@Composable
private fun DiscardButton(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.active_walk_discard),
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colors.danger,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
    )
}

/** "Started 07:05 · Anna · Harness", or nothing until the walk has loaded. */
@Composable
private fun ActiveWalkUiState.startedSummary(): String {
    val start = startTime ?: return ""
    val parts = buildList {
        add(stringResource(R.string.active_walk_started_at, start.format(TimeFormatter)))
        addAll(handlers)
        equipment?.let { add(stringResource(it.labelRes)) }
    }
    return parts.joinToString(" · ")
}

/** "34:12", or "1:02:05" once the walk passes an hour. */
private fun Duration.toClockText(): String {
    val hours = toHours()
    val minutes = toMinutes() % 60
    val seconds = seconds % 60
    return if (hours >
        0
    ) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Preview(heightDp = 800)
@Composable
private fun ActiveWalkScreenPreview() {
    WalkLoggerTheme {
        ActiveWalkScreen(
            uiState = LogWalkPreviewData.activeWalk,
            onNoteChange = {},
            onStoolPassedChange = {},
            onStoolQuantitySelect = {},
            onStoolTextureSelect = {},
            onStoolColourSelect = {},
            onStoolFold = {},
            onStoolExpand = {},
            onEndWalk = {},
            onDiscardWalk = {},
        )
    }
}

@Preview(heightDp = 800)
@Composable
private fun ActiveWalkScreenLoadingPreview() {
    WalkLoggerTheme {
        ActiveWalkScreen(
            uiState = ActiveWalkUiState(),
            onNoteChange = {},
            onStoolPassedChange = {},
            onStoolQuantitySelect = {},
            onStoolTextureSelect = {},
            onStoolColourSelect = {},
            onStoolFold = {},
            onStoolExpand = {},
            onEndWalk = {},
            onDiscardWalk = {},
        )
    }
}
