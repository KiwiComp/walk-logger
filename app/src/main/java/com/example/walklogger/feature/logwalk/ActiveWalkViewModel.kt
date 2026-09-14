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
    private val ending = MutableStateFlow(EndState())

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MILLIS)
        }
    }

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            note.filterNotNull().debounce(NOTE_SAVE_DELAY_MILLIS).collect { walkRepository.updateNotes(walkId, it) }
        }
    }

    val uiState: StateFlow<ActiveWalkUiState> =
        combine(
            walkRepository.observeWalk(walkId).filterNotNull(),
            dogRepository.observeActiveDog(),
            ticks,
            note,
            ending,
        ) { walk, dog, now, note, ending ->
            ActiveWalkUiState(
                dogName = dog?.name,
                handlers = walk.handlers,
                equipment = walk.equipment,
                startTime = walk.startInstant().atZone(clock.zone).toLocalTime(),
                elapsed = Duration.between(walk.startInstant(), now),
                note = note ?: walk.notes,
                isEnding = ending.isEnding,
                isEnded = ending.isEnded,
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
        ending.update { it.copy(isEnding = true) }
        viewModelScope.launch {
            walkRepository.updateNotes(walkId, state.note)
            walkRepository.finishWalk(walkId)
            ending.update { EndState(isEnding = false, isEnded = true) }
        }
    }

    private fun Walk.startInstant(): Instant = Instant.ofEpochMilli(startTime)

    private data class EndState(val isEnding: Boolean = false, val isEnded: Boolean = false)

    private companion object {
        const val TICK_MILLIS = 1_000L
        const val NOTE_SAVE_DELAY_MILLIS = 500L
    }
}
