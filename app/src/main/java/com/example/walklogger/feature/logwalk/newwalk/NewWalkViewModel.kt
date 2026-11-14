package com.example.walklogger.feature.logwalk.newwalk

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.HandlerRepository
import com.example.walklogger.core.data.WalkLocationCapturer
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.ui.message.UserMessageState
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.feature.logwalk.HandlerSelectionEditor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** [date] is the day chosen on the log screen; passed in directly so tests need no SavedStateHandle. */
@HiltViewModel(assistedFactory = NewWalkViewModel.Factory::class)
class NewWalkViewModel @AssistedInject constructor(
    @Assisted private val date: LocalDate,
    private val dogRepository: DogRepository,
    handlerRepository: HandlerRepository,
    private val walkRepository: WalkRepository,
    private val walkLocationCapturer: WalkLocationCapturer,
    private val locationPermission: LocationPermissionUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(date: LocalDate): NewWalkViewModel
    }

    /** Declared before the editor, which is handed [UserMessageState.show]. */
    private val userMessage = UserMessageState()
    private val handlers =
        HandlerSelectionEditor(viewModelScope, handlerRepository, userMessage::show)
    private val equipment = MutableStateFlow(Equipment.COLLAR)
    private val start = MutableStateFlow(StartState())

    init {
        handlers.preselectFirst()
    }

    val uiState: StateFlow<NewWalkUiState> =
        combine(
            dogRepository.observeActiveDog(),
            handlers.state,
            equipment,
            start,
        ) { dog, handlers, equipment, start ->
            NewWalkUiState(
                date = date,
                dogName = dog?.name,
                handlers = handlers.handlers,
                selectedHandlers = handlers.selected,
                equipment = equipment,
                isStarting = start.isStarting,
                shouldRequestLocationPermission = start.shouldRequestLocationPermission,
                startedWalkId = start.walkId,
            )
        }.combine(userMessage.message) { state, message -> state.copy(userMessage = message) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewWalkUiState(date = date),
            )

    fun toggleHandler(handler: String) = handlers.toggle(handler)

    /** Stores the name (or finds the existing one) and selects it. */
    fun addHandler(name: String) = handlers.add(name)

    fun selectEquipment(value: Equipment) = equipment.update { value }

    /**
     * Stores the walk with the start time set to now; [NewWalkUiState.startedWalkId] follows. The first
     * start asks for location instead and stores the walk once the user has answered.
     */
    fun startWalk() {
        if (!uiState.value.canStart) return
        start.update { it.copy(isStarting = true) }
        viewModelScope.launch {
            if (locationPermission.shouldRequest()) {
                start.update { it.copy(shouldRequestLocationPermission = true) }
            } else {
                storeWalk()
            }
        }
    }

    /** The walk starts whatever the answer; without permission the weather falls back to the saved place. */
    fun onLocationPermissionAnswered() {
        if (!start.value.shouldRequestLocationPermission) return
        start.update { it.copy(shouldRequestLocationPermission = false) }
        viewModelScope.launch {
            runCatchingCancellable { locationPermission.markRequested() }
            storeWalk()
        }
    }

    fun userMessageShown(@StringRes message: Int) = userMessage.shown(message)

    private suspend fun storeWalk() {
        val state = uiState.value
        val selected = handlers.state.value.selectedInOrder
        runCatchingCancellable {
            val dog =
                checkNotNull(dogRepository.observeActiveDog().first()) {
                    "starting a walk needs an active dog"
                }
            walkRepository.startWalk(
                dogId = dog.id,
                handlers = selected,
                equipment = state.equipment,
            )
        }
            .onSuccess { walkId ->
                walkLocationCapturer.capture(walkId)
                start.update { StartState(walkId = walkId) }
            }
            .onFailure {
                start.update { StartState() }
                userMessage.show(R.string.error_start_walk)
            }
    }

    private data class StartState(
        val isStarting: Boolean = false,
        val shouldRequestLocationPermission: Boolean = false,
        val walkId: Long? = null,
    )
}
