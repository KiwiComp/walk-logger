package com.example.walklogger.feature.logwalk

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppButtonStyle
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.component.AppTextFieldStyle
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.Navy
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.OnNavyMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.Duration
import java.time.format.DateTimeFormatter

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private const val NoteMinLines = 2

/** The ways off this screen are ending or discarding the walk; system back asks to discard. */
@Composable
fun ActiveWalkRoute(
    walkId: Long,
    onWalkEnded: (walkId: Long) -> Unit,
    onWalkDiscarded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveWalkViewModel = hiltViewModel<ActiveWalkViewModel, ActiveWalkViewModel.Factory> { it.create(walkId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isEnded, uiState.isDiscarded) {
        when {
            uiState.isEnded -> onWalkEnded(walkId)
            uiState.isDiscarded -> onWalkDiscarded()
        }
    }
    ActiveWalkScreen(
        uiState = uiState,
        onNoteChange = viewModel::setNote,
        onEndWalk = viewModel::endWalk,
        onDiscardWalk = viewModel::discardWalk,
        modifier = modifier,
    )
}

@Composable
fun ActiveWalkScreen(
    uiState: ActiveWalkUiState,
    onNoteChange: (String) -> Unit,
    onEndWalk: () -> Unit,
    onDiscardWalk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }
    BackHandler { showDiscardConfirm = uiState.canDiscard }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = AppSpacing.xxxl)
            .padding(top = AppSpacing.xxl, bottom = AppSpacing.xxl),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel(
                    text = uiState.dogName?.let { stringResource(R.string.active_walk_out_with, it) }
                        ?: stringResource(R.string.active_walk_title),
                    color = OnNavyMuted,
                )
                HeaderIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.active_walk_discard),
                    onClick = { showDiscardConfirm = true },
                    enabled = uiState.canDiscard,
                )
            }
            Spacer(Modifier.height(AppSpacing.md))
            Text(
                text = uiState.elapsed.toClockText(),
                style = MaterialTheme.typography.displayLarge,
                color = OnNavy,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                text = uiState.startedSummary(),
                style = MaterialTheme.typography.bodyLarge,
                color = OnNavyMuted,
            )
            Spacer(Modifier.height(AppSpacing.xxxl))
            SectionLabel(text = stringResource(R.string.active_walk_quick_note), color = OnNavyMuted)
            Spacer(Modifier.height(AppSpacing.sm))
            AppTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                style = AppTextFieldStyle.Inverse,
                placeholder = stringResource(R.string.active_walk_quick_note_placeholder),
                singleLine = false,
                minLines = NoteMinLines,
            )
        }
        Spacer(Modifier.height(AppSpacing.xl))
        AppButton(
            text = stringResource(R.string.active_walk_end),
            onClick = onEndWalk,
            style = AppButtonStyle.Secondary,
            enabled = uiState.canEnd || uiState.isEnding,
            loading = uiState.isEnding,
        )
    }
    if (showDiscardConfirm) {
        DiscardWalkDialog(
            onConfirm = {
                showDiscardConfirm = false
                onDiscardWalk()
            },
            onDismiss = { showDiscardConfirm = false },
        )
    }
}

/** "Started 07:05 · Anna · harness", or nothing until the walk has loaded. */
@Composable
private fun ActiveWalkUiState.startedSummary(): String {
    val start = startTime ?: return ""
    val locale = LocalConfiguration.current.locales[0]
    val parts = buildList {
        add(stringResource(R.string.active_walk_started_at, start.format(TimeFormatter)))
        addAll(handlers)
        equipment?.let { add(stringResource(it.labelRes).lowercase(locale)) }
    }
    return parts.joinToString(" · ")
}

/** "34:12", or "1:02:05" once the walk passes an hour. */
private fun Duration.toClockText(): String {
    val hours = toHours()
    val minutes = toMinutes() % 60
    val seconds = seconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}

@Preview(heightDp = 800)
@Composable
private fun ActiveWalkScreenPreview() {
    WalkLoggerTheme {
        ActiveWalkScreen(uiState = LogWalkPreviewData.activeWalk, onNoteChange = {}, onEndWalk = {}, onDiscardWalk = {})
    }
}

@Preview(heightDp = 800)
@Composable
private fun ActiveWalkScreenLoadingPreview() {
    WalkLoggerTheme {
        ActiveWalkScreen(uiState = ActiveWalkUiState(), onNoteChange = {}, onEndWalk = {}, onDiscardWalk = {})
    }
}
