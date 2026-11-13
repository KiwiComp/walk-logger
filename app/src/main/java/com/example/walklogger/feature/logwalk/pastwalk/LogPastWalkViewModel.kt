package com.example.walklogger.feature.logwalk.pastwalk

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.HandlerRepository
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.message.UserMessageState
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
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

/**
 * [date] is the day chosen on the log screen; passed in directly so tests need no SavedStateHandle.
 * With a [walkId] the form is prefilled from that walk and saving updates it instead of inserting.
 */
@HiltViewModel(assistedFactory = LogPastWalkViewModel.Factory::class)
class LogPastWalkViewModel @AssistedInject constructor(
    @Assisted date: LocalDate,
    @Assisted private val walkId: Long?,
    dogRepository: DogRepository,
    handlerRepository: HandlerRepository,
    behaviourIssueRepository: BehaviourIssueRepository,
    medicalConcernRepository: MedicalConcernRepository,
    private val walkRepository: WalkRepository,
    clock: Clock,
    fetchWalkWeather: FetchWalkWeatherUseCase,
    locationPermission: LocationPermissionUseCase,
    locationRepository: LocationRepository,
    private val saveWalk: SaveWalkUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(date: LocalDate, walkId: Long?): LogPastWalkViewModel
    }

    /** Declared before the editors, which are handed [UserMessageState.show]. */
    private val userMessage = UserMessageState()

    private val form = WalkFormEditor(
        viewModelScope,
        behaviourIssueRepository,
        medicalConcernRepository,
        userMessage::show,
        fetchWalkWeather,
        locationPermission,
        locationRepository,
    )

    val formActions: WalkFormActions get() = form

    /** A new walk fetches the weather for its times; an edited one only through the fetch button. */
    private val times = WalkTimesEditor(clock, date) { start, end ->
        form.onTimesChanged(start, end, autoFetch = walkId == null)
    }

    private val handlers =
        HandlerSelectionEditor(viewModelScope, handlerRepository, userMessage::show)

    private val screen = MutableStateFlow(
        LogPastWalkUiState(
            times = times.state.value,
            isEditing = walkId != null,
            isLoading = walkId != null,
        ),
    )

    /** The walk being edited, kept so saving preserves its id and dog. */
    private var editedWalk: Walk? = null

    init {
        viewModelScope.launch {
            if (walkId == null) {
                handlers.preselectFirst()
                return@launch
            }
            val walk = walkRepository.observeWalk(walkId).first()
            if (walk == null) {
                screen.update { it.copy(isNotFound = true) }
                return@launch
            }
            editedWalk = walk
            form.prefillFrom(walk)
            handlers.prefill(walk.handlers)
            screen.update { it.copy(isLoading = false) }
            times.prefillFrom(walk)
        }
    }

    val uiState: StateFlow<LogPastWalkUiState> =
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
                dogId = dog?.id,
                dogName = dog?.name,
                handlers = handlers.handlers,
                selectedHandlers = handlers.selected,
            )
        }.combine(userMessage.message) { state, message -> state.copy(userMessage = message) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = screen.value.copy(form = form.state.value),
            )

    fun userMessageShown(@StringRes message: Int) = userMessage.shown(message)

    fun setDate(date: LocalDate) = times.setDate(date)

    fun setStartTime(time: LocalTime) = times.setStartTime(time)

    fun setEndTime(time: LocalTime) = times.setEndTime(time)

    fun toggleHandler(handler: String) = handlers.toggle(handler)

    /** Stores the name (or finds the existing one) and selects it. */
    fun addHandler(name: String) = handlers.add(name)

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        val walk = Walk(
            id = editedWalk?.id ?: SaveWalkUseCase.UNSAVED,
            dogId = editedWalk?.dogId ?: checkNotNull(state.dogId),
            handlers = handlers.state.value.selectedInOrder,
            startTime = checkNotNull(times.startMillis),
            endTime = checkNotNull(times.endMillis),
        )
        screen.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatchingCancellable { saveWalk(form.applyTo(walk)) }
                .onSuccess { screen.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure {
                    screen.update { it.copy(isSaving = false) }
                    userMessage.show(R.string.error_save_walk)
                }
        }
    }
}
