package com.example.walklogger.feature.logwalk.activewalk

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.startLocalTime
import com.example.walklogger.core.model.withColour
import com.example.walklogger.core.model.withPassed
import com.example.walklogger.core.model.withQuantity
import com.example.walklogger.core.model.withTexture
import com.example.walklogger.domain.DiscardWalkUseCase
import com.example.walklogger.feature.logwalk.MAX_NOTES_LENGTH
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
    private val clock: Clock,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(walkId: Long): ActiveWalkViewModel
    }

    /** What the user has typed; null until they touch the field, so the stored note shows instead. */
    private val note = MutableStateFlow<String?>(null)

    /** What the user has picked; null until they touch the section, so the stored answer shows instead. */
    private val stool = MutableStateFlow<StoolEdit?>(null)

    /** Starts folded, so a walk reopened with an answer shows only its summary. */
    private val isStoolExpanded = MutableStateFlow(false)
    private val outcome = MutableStateFlow(Outcome())

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MILLIS)
        }
    }

    /** Cancelled on discard so a pending note can't be written after the walk is gone; restarted if the discard fails. */
    private var noteSaver = saveNotes()

    /** Taps are stored at once and in order; cancelled on discard like [noteSaver]. */
    private var stoolSaver = saveStool()

    init {
        viewModelScope.launch {
            if (walkRepository.observeWalk(walkId).first() == null) {
                outcome.update { it.copy(isNotFound = true) }
            }
        }
    }

    private val edits = combine(note, stool, isStoolExpanded, ::Edits)

    val uiState: StateFlow<ActiveWalkUiState> =
        combine(
            walkRepository.observeWalk(walkId),
            dogRepository.observeActiveDog(),
            ticks,
            edits,
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
            ActiveWalkUiState(
                dogName = dog?.name,
                handlers = walk.handlers,
                equipment = walk.equipment,
                startTime = walk.startLocalTime(clock.zone),
                elapsed = Duration.between(walk.startInstant(), now),
                note = edits.note ?: walk.notes,
                stool = if (edits.stool != null) edits.stool.stool else walk.stool,
                isStoolExpanded = edits.isStoolExpanded,
                isEnding = outcome.isEnding,
                isEnded = outcome.isEnded,
                isDiscarding = outcome.isDiscarding,
                isDiscarded = outcome.isDiscarded,
                userMessage = outcome.userMessage,
            )
        }.filterNotNull().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ActiveWalkUiState(),
        )

    fun setNote(text: String) = note.update { text.take(MAX_NOTES_LENGTH) }

    /** On opens the description rows; off means not answered, so the description is dropped. */
    fun setStoolPassed(passed: Boolean) {
        editStool { if (passed) it.withPassed(true) else null }
        isStoolExpanded.value = passed
    }

    fun foldStool() {
        isStoolExpanded.value = false
    }

    fun expandStool() {
        isStoolExpanded.value = true
    }

    fun selectStoolQuantity(value: StoolQuantity) = editStool { it.withQuantity(value) }

    fun selectStoolTexture(value: StoolTexture) = editStool { it.withTexture(value) }

    /** A null [value] removes the colour. */
    fun selectStoolColour(value: StoolColour?) = editStool { it.withColour(value) }

    private fun editStool(edit: (Stool?) -> Stool?) = stool.update {
        StoolEdit(
            edit(
                if (it !=
                    null
                ) {
                    it.stool
                } else {
                    uiState.value.stool
                },
            ),
        )
    }

    /** Stores the end time; the after-walk form then opens for the finished walk. */
    fun endWalk() {
        val state = uiState.value
        if (!state.canEnd) return
        outcome.update { it.copy(isEnding = true) }
        viewModelScope.launch {
            runCatchingCancellable { walkRepository.endWalk(walkId, state.note, state.stool) }
                .onSuccess { outcome.update { Outcome(isEnded = true) } }
                .onFailure {
                    outcome.update {
                        it.copy(isEnding = false, userMessage = R.string.error_end_walk)
                    }
                }
        }
    }

    /** Deletes the walk outright, note included; nothing is logged. */
    fun discardWalk() {
        if (!uiState.value.canDiscard) return
        outcome.update { it.copy(isDiscarding = true) }
        noteSaver.cancel()
        stoolSaver.cancel()
        viewModelScope.launch {
            runCatchingCancellable { discardWalkUseCase(walkId) }
                .onSuccess { outcome.update { Outcome(isDiscarded = true) } }
                .onFailure {
                    noteSaver = saveNotes()
                    stoolSaver = saveStool()
                    outcome.update {
                        it.copy(isDiscarding = false, userMessage = R.string.error_delete_walk)
                    }
                }
        }
    }

    fun userMessageShown(@StringRes message: Int) = outcome.update {
        if (it.userMessage == message) it.copy(userMessage = null) else it
    }

    private fun showMessage(@StringRes message: Int) = outcome.update {
        it.copy(userMessage = message)
    }

    private fun saveNotes() = viewModelScope.launch {
        @OptIn(FlowPreview::class)
        note.filterNotNull().debounce(NOTE_SAVE_DELAY_MILLIS).collect {
            runCatchingCancellable { walkRepository.updateNotes(walkId, it) }
                .onFailure { showMessage(R.string.error_save_note) }
        }
    }

    private fun saveStool() = viewModelScope.launch {
        stool.filterNotNull().collect {
            runCatchingCancellable { walkRepository.updateStool(walkId, it.stool) }
                .onFailure { showMessage(R.string.error_save_stool) }
        }
    }

    private fun Walk.startInstant(): Instant = Instant.ofEpochMilli(startTime)

    /** Wraps the pick so a cleared answer (null) can be told apart from an untouched section. */
    private data class StoolEdit(val stool: Stool?)

    private data class Edits(val note: String?, val stool: StoolEdit?, val isStoolExpanded: Boolean)

    private data class Outcome(
        val isEnding: Boolean = false,
        val isEnded: Boolean = false,
        val isDiscarding: Boolean = false,
        val isDiscarded: Boolean = false,
        val isNotFound: Boolean = false,
        @param:StringRes val userMessage: Int? = null,
    )

    private companion object {
        const val TICK_MILLIS = 1_000L
        const val NOTE_SAVE_DELAY_MILLIS = 500L
    }
}
