package com.example.walklogger.feature.logwalk.activewalk

import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.withColour
import com.example.walklogger.core.model.withPassed
import com.example.walklogger.core.model.withQuantity
import com.example.walklogger.core.model.withTexture
import com.example.walklogger.feature.logwalk.MAX_NOTES_LENGTH
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveWalkEdits(val note: String, val stool: Stool?, val isStoolExpanded: Boolean)

class ActiveWalkEditor(
    private val scope: CoroutineScope,
    private val walkId: Long,
    stored: Flow<Walk?>,
    private val walkRepository: WalkRepository,
    private val showError: (Throwable, Int, String) -> Unit,
) {
    /** What the user has typed; null until they touch the field, so the stored note shows instead. */
    private val note = MutableStateFlow<String?>(null)

    /** What the user has picked; null until they touch the section, so the stored answer shows instead. */
    private val stool = MutableStateFlow<StoolEdit?>(null)

    /** Starts folded, so a walk reopened with an answer shows only its summary. */
    private val isStoolExpanded = MutableStateFlow(false)

    /** Cancelled on discard so a pending note can't be written after the walk is gone; restarted if the discard fails. */
    private var noteSaver = saveNotes()

    /** Taps are stored at once and in order; cancelled on discard like [noteSaver]. */
    private var stoolSaver = saveStool()

    /**
     * The stored walk with the user's edits on top; null while there is no walk.
     * Eager so the edit functions can read it while nothing collects.
     */
    val state: StateFlow<ActiveWalkEdits?> = combine(stored, note, stool, isStoolExpanded) {
            walk,
            note,
            stool,
            isStoolExpanded,
        ->
        walk?.let {
            ActiveWalkEdits(
                note = note ?: it.notes,
                stool = if (stool != null) stool.stool else it.stool,
                isStoolExpanded = isStoolExpanded,
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, null)

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

    fun pause() {
        noteSaver.cancel()
        stoolSaver.cancel()
    }
    fun resume() {
        noteSaver = saveNotes()
        stoolSaver = saveStool()
    }

    private fun editStool(edit: (Stool?) -> Stool?) = stool.update {
        StoolEdit(
            edit(
                if (it != null) {
                    it.stool
                } else {
                    state.value?.stool
                },
            ),
        )
    }

    private fun saveNotes() = scope.launch {
        @OptIn(FlowPreview::class)
        note.filterNotNull().debounce(NOTE_SAVE_DELAY).collect {
            runCatchingCancellable { walkRepository.updateNotes(walkId, it) }
                .onFailure {
                    showError(
                        it,
                        R.string.error_save_note,
                        "Failed to save note",
                    )
                }
        }
    }

    private fun saveStool() = scope.launch {
        stool.filterNotNull().collect {
            runCatchingCancellable { walkRepository.updateStool(walkId, it.stool) }
                .onFailure {
                    showError(
                        it,
                        R.string.error_save_stool,
                        "Failed to save stool answer",
                    )
                }
        }
    }

    private companion object {
        val NOTE_SAVE_DELAY = 500.milliseconds
    }

    /** Wraps the pick so a cleared answer (null) can be told apart from an untouched section. */
    private data class StoolEdit(val stool: Stool?)
}
