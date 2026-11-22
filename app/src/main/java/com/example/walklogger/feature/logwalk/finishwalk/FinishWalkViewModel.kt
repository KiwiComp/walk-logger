package com.example.walklogger.feature.logwalk.finishwalk

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.HandlerRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.data.WalkRouteRepository
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.within
import com.example.walklogger.core.ui.message.UserMessageState
import com.example.walklogger.domain.DiscardWalkUseCase
import com.example.walklogger.domain.SaveWalkUseCase
import com.example.walklogger.feature.logwalk.HandlerSelectionEditor
import com.example.walklogger.feature.logwalk.WalkFormActions
import com.example.walklogger.feature.logwalk.WalkFormEditor
import com.example.walklogger.feature.logwalk.WalkTimesEditor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Rates and describes the walk with [walkId], which the timer screen has already finished. */
@HiltViewModel(assistedFactory = FinishWalkViewModel.Factory::class)
class FinishWalkViewModel @AssistedInject constructor(
    @Assisted private val walkId: Long,
    dogRepository: DogRepository,
    handlerRepository: HandlerRepository,
    private val walkRepository: WalkRepository,
    walkRouteRepository: WalkRouteRepository,
    clock: Clock,
    formFactory: WalkFormEditor.Factory,
    private val saveWalk: SaveWalkUseCase,
    private val discardWalk: DiscardWalkUseCase,
    errorReporter: ErrorReporter,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(walkId: Long): FinishWalkViewModel
    }

    /** Declared before the editors, which are handed [UserMessageState.showAndReportError]. */
    private val userMessage = UserMessageState(errorReporter)

    private val form = formFactory.create(viewModelScope, userMessage::showAndReportError, walkId)

    val formActions: WalkFormActions get() = form

    private val handlers =
        HandlerSelectionEditor(viewModelScope, handlerRepository, userMessage::showAndReportError)

    private val times = WalkTimesEditor(clock, LocalDate.now(clock)) { start, end ->
        form.onTimesChanged(start, end, autoFetch = true)
    }

    private val screen = MutableStateFlow(FinishWalkUiState(times = times.state.value))

    /** Follows the edited times, so points hidden by a shorter walk show again when it is lengthened. */
    private val route =
        combine(walkRouteRepository.observeRoute(walkId), times.state) { points, _ ->
            val start = times.startMillis ?: return@combine emptyList()
            points.within(start, times.endMillis).map { it.coordinates }
        }

    /** The stored walk, kept so saving preserves its id, dog and handlers. */
    private var walk: Walk? = null

    init {
        viewModelScope.launch {
            val stored = walkRepository.observeWalk(walkId).first()
            if (stored == null) {
                screen.update { it.copy(isNotFound = true) }
                return@launch
            }
            walk = stored
            form.prefillFrom(stored)
            handlers.prefill(stored.handlers)
            screen.update { it.copy(isLoading = false) }
            times.prefillFrom(stored)
        }
    }

    val uiState: StateFlow<FinishWalkUiState> =
        combine(
            screen,
            times.state,
            form.state,
            dogRepository.observeActiveDog(),
            handlers.state,
        ) { screen, times, form, dog, handlers ->
            screen.copy(
                times = times,
                form = form,
                dogName = dog?.name,
                handlers = handlers.handlers,
                selectedHandlers = handlers.selected,
            )
        }.combine(route) { state, route -> state.copy(route = route) }
            .combine(userMessage.message) { state, message -> state.copy(userMessage = message) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = screen.value.copy(form = form.state.value),
            )

    fun userMessageShown(@StringRes message: Int) = userMessage.shown(message)

    fun toggleEditTimes() = screen.update { it.copy(isEditingTimes = !it.isEditingTimes) }

    fun toggleEditHandlers() = screen.update { it.copy(isEditingHandlers = !it.isEditingHandlers) }

    fun toggleHandler(handler: String) = handlers.toggle(handler)

    /** Stores the name (or finds the existing one) and selects it. */
    fun addHandler(name: String) = handlers.add(name)

    fun setDate(date: LocalDate) = times.setDate(date)

    fun setStartTime(time: LocalTime) = times.setStartTime(time)

    fun setEndTime(time: LocalTime) = times.setEndTime(time)

    fun save() {
        if (!uiState.value.canSave) return
        val stored = checkNotNull(walk) { "walk $walkId has not loaded" }
        val start = checkNotNull(times.startMillis)
        val end = checkNotNull(times.endMillis)
        screen.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            form.awaitWeather()
            runCatchingCancellable {
                saveWalk(
                    form.applyTo(
                        stored.copy(
                            startTime = start,
                            endTime = end,
                            handlers = handlers.state.value.selectedInOrder,
                        ),
                    ),
                )
            }
                .onSuccess { screen.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure {
                    screen.update { it.copy(isSaving = false) }
                    userMessage.showAndReportError(
                        it,
                        R.string.error_save_walk,
                        "Failed to save walk",
                    )
                }
        }
    }

    /** Deletes the walk outright; nothing is logged. */
    fun discard() {
        if (!uiState.value.canDiscard) return
        screen.update { it.copy(isDiscarding = true) }
        viewModelScope.launch {
            runCatchingCancellable { discardWalk(walkId) }
                .onSuccess { screen.update { it.copy(isDiscarding = false, isDiscarded = true) } }
                .onFailure {
                    screen.update { it.copy(isDiscarding = false) }
                    userMessage.showAndReportError(
                        it,
                        R.string.error_delete_walk,
                        "Failed to discard walk",
                    )
                }
        }
    }
}
