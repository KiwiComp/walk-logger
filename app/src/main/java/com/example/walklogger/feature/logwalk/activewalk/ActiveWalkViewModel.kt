package com.example.walklogger.feature.logwalk.activewalk

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.data.WalkTracker
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.startLocalTime
import com.example.walklogger.core.ui.message.UserMessageState
import com.example.walklogger.domain.DiscardWalkUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The walk in progress. Elapsed time is derived from the stored start time on every tick,
 * so it stays right after the app has been in the background or its process was killed.
 */
@HiltViewModel(assistedFactory = ActiveWalkViewModel.Factory::class)
class ActiveWalkViewModel @AssistedInject constructor(
    @Assisted private val walkId: Long,
    dogRepository: DogRepository,
    private val walkRepository: WalkRepository,
    private val discardWalkUseCase: DiscardWalkUseCase,
    private val walkTracker: WalkTracker,
    private val clock: Clock,
    errorReporter: ErrorReporter,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(walkId: Long): ActiveWalkViewModel
    }

    /** [walk] and [userMessage] are declared before the editor, which is handed both. */
    private val walk = walkRepository.observeWalk(walkId)
    private val userMessage = UserMessageState(errorReporter)
    private val editor =
        ActiveWalkEditor(
            viewModelScope,
            walkId,
            walk,
            walkRepository,
            userMessage::showAndReportError,
        )
    private val outcome = MutableStateFlow(Outcome())

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_INTERVAL)
        }
    }

    init {
        viewModelScope.launch {
            val stored = walk.first()
            when {
                stored == null -> outcome.update { it.copy(isNotFound = true) }

                // Resumes the route after Android killed the app; the screen is visible, so the service may start.
                stored.isActive -> walkTracker.track(walkId)
            }
        }
    }

    val uiState: StateFlow<ActiveWalkUiState> =
        combine(
            walk,
            dogRepository.observeActiveDog(),
            ticks,
            editor.state,
            outcome,
        ) { walk, dog, now, edits, outcome ->
            // A walk deleted by discardWalk keeps showing its last state until the screen leaves.
            if (walk == null) {
                return@combine when {
                    outcome.isNotFound -> ActiveWalkUiState(isNotFound = true)
                    outcome.isDiscarded -> ActiveWalkUiState(isDiscarded = true)
                    else -> null
                }
            }
            if (edits == null) return@combine null
            ActiveWalkUiState(
                dogName = dog?.name,
                handlers = walk.handlers,
                equipment = walk.equipment,
                startTime = walk.startLocalTime(clock.zone),
                elapsed = Duration.between(walk.startInstant(), now),
                note = edits.note,
                stool = edits.stool,
                isStoolExpanded = edits.isStoolExpanded,
                isEnding = outcome.isEnding,
                isEnded = outcome.isEnded,
                isDiscarding = outcome.isDiscarding,
                isDiscarded = outcome.isDiscarded,
            )
        }.filterNotNull()
            .combine(userMessage.message) { state, message -> state.copy(userMessage = message) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ActiveWalkUiState(),
            )

    fun setNote(text: String) = editor.setNote(text)

    fun setStoolPassed(passed: Boolean) = editor.setStoolPassed(passed)

    fun foldStool() = editor.foldStool()

    fun expandStool() = editor.expandStool()

    fun selectStoolQuantity(value: StoolQuantity) = editor.selectStoolQuantity(value)

    fun selectStoolTexture(value: StoolTexture) = editor.selectStoolTexture(value)

    fun selectStoolColour(value: StoolColour?) = editor.selectStoolColour(value)

    /** Stores the end time; the after-walk form then opens for the finished walk. */
    fun endWalk() {
        val state = uiState.value
        if (!state.canEnd) return
        outcome.update { it.copy(isEnding = true) }
        viewModelScope.launch {
            runCatchingCancellable { walkRepository.endWalk(walkId, state.note, state.stool) }
                .onSuccess { outcome.update { Outcome(isEnded = true) } }
                .onFailure {
                    outcome.update { it.copy(isEnding = false) }
                    userMessage.showAndReportError(
                        it,
                        R.string.error_end_walk,
                        "Failed to end walk",
                    )
                }
        }
    }

    /** Deletes the walk outright, note included; nothing is logged. */
    fun discardWalk() {
        if (!uiState.value.canDiscard) return
        outcome.update { it.copy(isDiscarding = true) }
        editor.pause()
        viewModelScope.launch {
            runCatchingCancellable { discardWalkUseCase(walkId) }
                .onSuccess { outcome.update { Outcome(isDiscarded = true) } }
                .onFailure {
                    editor.resume()
                    outcome.update { it.copy(isDiscarding = false) }
                    userMessage.showAndReportError(
                        it,
                        R.string.error_delete_walk,
                        "Failed to discard walk",
                    )
                }
        }
    }

    fun userMessageShown(@StringRes message: Int) = userMessage.shown(message)

    private fun Walk.startInstant(): Instant = Instant.ofEpochMilli(startTime)

    private data class Outcome(
        val isEnding: Boolean = false,
        val isEnded: Boolean = false,
        val isDiscarding: Boolean = false,
        val isDiscarded: Boolean = false,
        val isNotFound: Boolean = false,
    )

    private companion object {
        val TICK_INTERVAL = 1.seconds
    }
}
