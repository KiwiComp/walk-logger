package com.example.walklogger.feature.logwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
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
    private val clock: Clock,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(walkId: Long): ActiveWalkViewModel
    }

    /** What the user has typed; null until they touch the field, so the stored note shows instead. */
    private val note = MutableStateFlow<String?>(null)
    private val outcome = MutableStateFlow(Outcome())

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MILLIS)
        }
    }

    /** Cancelled on discard so a pending note can't be written after the walk is gone. */
    private val noteSaver = viewModelScope.launch {
        @OptIn(FlowPreview::class)
        note.filterNotNull().debounce(NOTE_SAVE_DELAY_MILLIS).collect { walkRepository.updateNotes(walkId, it) }
    }

    val uiState: StateFlow<ActiveWalkUiState> =
        combine(
            walkRepository.observeWalk(walkId).filterNotNull(),
            dogRepository.observeActiveDog(),
            ticks,
            note,
            outcome,
        ) { walk, dog, now, note, outcome ->
            ActiveWalkUiState(
                dogName = dog?.name,
                handlers = walk.handlers,
                equipment = walk.equipment,
                startTime = walk.startInstant().atZone(clock.zone).toLocalTime(),
                elapsed = Duration.between(walk.startInstant(), now),
                note = note ?: walk.notes,
                isEnding = outcome.isEnding,
                isEnded = outcome.isEnded,
                isDiscarding = outcome.isDiscarding,
                isDiscarded = outcome.isDiscarded,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ActiveWalkUiState(),
        )

    fun setNote(text: String) = note.update { text.take(LogPastWalkViewModel.MAX_NOTES_LENGTH) }

    /** Stores the end time; the after-walk form then opens for the finished walk. */
    fun endWalk() {
        val state = uiState.value
        if (!state.canEnd) return
        outcome.update { it.copy(isEnding = true) }
        viewModelScope.launch {
            walkRepository.updateNotes(walkId, state.note)
            walkRepository.finishWalk(walkId)
            outcome.update { Outcome(isEnded = true) }
        }
    }

    /** Deletes the walk outright, note included; nothing is logged. */
    fun discardWalk() {
        if (!uiState.value.canDiscard) return
        outcome.update { it.copy(isDiscarding = true) }
        noteSaver.cancel()
        viewModelScope.launch {
            walkRepository.deleteWalk(walkId)
            outcome.update { Outcome(isDiscarded = true) }
        }
    }

    private fun Walk.startInstant(): Instant = Instant.ofEpochMilli(startTime)

    private data class Outcome(
        val isEnding: Boolean = false,
        val isEnded: Boolean = false,
        val isDiscarding: Boolean = false,
        val isDiscarded: Boolean = false,
    )

    private companion object {
        const val TICK_MILLIS = 1_000L
        const val NOTE_SAVE_DELAY_MILLIS = 500L
    }
}
