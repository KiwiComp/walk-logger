package com.example.walklogger.feature.logwalk

import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.HandlerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HandlerSelectionState(
    val handlers: List<String> = emptyList(),
    val selected: Set<String> = emptySet(),
) {
    /** The selected handlers in stored order, as a walk keeps them. */
    val selectedInOrder: List<String> get() = handlers.filter { it in selected }
}

/** Holds which of the stored handlers walk a new or edited walk, for its ViewModel. */
class HandlerSelectionEditor(
    private val scope: CoroutineScope,
    private val handlerRepository: HandlerRepository,
    private val showError: (Throwable, Int, String) -> Unit,
) {
    private val selected = MutableStateFlow<Set<String>>(emptySet())

    val state: StateFlow<HandlerSelectionState> =
        combine(handlerRepository.observeHandlers(), selected, ::HandlerSelectionState)
            .stateIn(scope, SharingStarted.Eagerly, HandlerSelectionState())

    /** The first stored handler is the usual walker, so it starts selected unless something already is. */
    fun preselectFirst() {
        scope.launch {
            handlerRepository.observeHandlers().first().firstOrNull()?.let { first ->
                selected.update { if (it.isEmpty()) setOf(first) else it }
            }
        }
    }

    fun prefill(handlers: List<String>) {
        selected.value = handlers.toSet()
    }

    fun toggle(handler: String) = selected.update {
        if (handler in it) it - handler else it + handler
    }

    /** Stores the name (or finds the existing one) and selects it. */
    fun add(name: String) {
        scope.launch {
            runCatchingCancellable { handlerRepository.addHandler(name) }
                .onSuccess { stored -> selected.update { it + stored } }
                .onFailure { showError(it, R.string.error_add_name, "Failed to add handler") }
        }
    }
}
