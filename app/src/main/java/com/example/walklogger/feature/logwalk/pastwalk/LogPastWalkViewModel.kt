package com.example.walklogger.feature.logwalk.pastwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.HandlerRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.data.PlaceRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.feature.logwalk.WalkFormActions
import com.example.walklogger.feature.logwalk.WalkFormEditor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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
    private val dogRepository: DogRepository,
    private val handlerRepository: HandlerRepository,
    behaviourIssueRepository: BehaviourIssueRepository,
    medicalConcernRepository: MedicalConcernRepository,
    private val walkRepository: WalkRepository,
    private val userMessages: UserMessages,
    private val clock: Clock,
    fetchWalkWeather: FetchWalkWeatherUseCase,
    locationPermission: LocationPermissionUseCase,
    placeRepository: PlaceRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(date: LocalDate, walkId: Long?): LogPastWalkViewModel
    }

    private val form = WalkFormEditor(
        viewModelScope,
        behaviourIssueRepository,
        medicalConcernRepository,
        userMessages,
        fetchWalkWeather,
        locationPermission,
        placeRepository,
    )

    val formActions: WalkFormActions get() = form

    /** The times and handlers; the dog and handler list are overlaid from their repositories. */
    private val screen = MutableStateFlow(
        LogPastWalkUiState(
            date = date,
            now = LocalDateTime.now(clock),
            isEditing = walkId != null,
            isLoading = walkId != null,
        ),
    )

    /** The walk being edited, kept so saving preserves its id and dog. */
    private var editedWalk: Walk? = null

    // Editing prefills from the stored walk; a new walk starts with the first (usual) handler selected.
    init {
        viewModelScope.launch {
            if (walkId != null) {
                val walk = walkRepository.observeWalk(walkId).first()
                if (walk == null) {
                    screen.update { it.copy(isNotFound = true) }
                    userMessages.show(R.string.error_walk_not_found)
                    return@launch
                }
                editedWalk = walk
                form.prefillFrom(walk)
                screen.update { it.prefilledFrom(walk) }
                onTimesChanged()
            } else {
                handlerRepository.observeHandlers().first().firstOrNull()?.let { first ->
                    screen.update {
                        if (it.selectedHandlers.isEmpty()) {
                            it.copy(
                                selectedHandlers = setOf(first),
                            )
                        } else {
                            it
                        }
                    }
                }
            }
        }
    }

    val uiState: StateFlow<LogPastWalkUiState> =
        combine(
            screen,
            form.state,
            dogRepository.observeActiveDog(),
            handlerRepository.observeHandlers(),
        ) { screen, form, dog, handlers ->
            screen.copy(form = form, dogName = dog?.name, handlers = handlers)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = screen.value.copy(form = form.state.value),
        )

    fun setDate(date: LocalDate) = updateWhen { it.copy(date = date) }

    fun setStartTime(time: LocalTime) = updateWhen { it.copy(startTime = time) }

    fun setEndTime(time: LocalTime) = updateWhen { it.copy(endTime = time) }

    /** Re-reads the clock alongside the edit so a form left open still validates against the present. */
    private fun updateWhen(edit: (LogPastWalkUiState) -> LogPastWalkUiState) {
        screen.update { edit(it).copy(now = LocalDateTime.now(clock)) }
        onTimesChanged()
    }

    /** A new walk fetches the weather for its times; an edited one only through the fetch button. */
    private fun onTimesChanged() {
        val state = screen.value
        val start = state.startTime ?: return
        val end = state.endTime ?: return
        form.onTimesChanged(
            state.date.atTime(start).toEpochMilli(),
            state.date.atTime(end).toEpochMilli(),
            autoFetch =
                walkId == null,
        )
    }

    fun toggleHandler(handler: String) = screen.update { state ->
        val selected = state.selectedHandlers
        state.copy(
            selectedHandlers = if (handler in
                selected
            ) {
                selected - handler
            } else {
                selected + handler
            },
        )
    }

    /** Stores the name (or finds the existing one) and selects it. */
    fun addHandler(name: String) {
        viewModelScope.launch {
            runCatchingCancellable { handlerRepository.addHandler(name) }
                .onSuccess { stored ->
                    screen.update {
                        it.copy(
                            selectedHandlers =
                                it.selectedHandlers + stored,
                        )
                    }
                }
                .onFailure { userMessages.show(R.string.error_add_name) }
        }
    }

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        screen.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatchingCancellable {
                val edited = editedWalk
                val dogId = edited?.dogId
                    ?: checkNotNull(dogRepository.observeActiveDog().first()) {
                        "log past walk needs an active dog"
                    }.id
                val walk = form.applyTo(
                    Walk(
                        id = edited?.id ?: 0,
                        dogId = dogId,
                        handlers = state.handlers.filter { it in state.selectedHandlers },
                        startTime = state.date.atTime(state.startTime!!).toEpochMilli(),
                        endTime = state.date.atTime(state.endTime!!).toEpochMilli(),
                    ),
                )
                if (edited !=
                    null
                ) {
                    walkRepository.updateWalk(walk)
                } else {
                    walkRepository.logWalk(walk)
                }
            }
                .onSuccess { screen.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure {
                    screen.update { it.copy(isSaving = false) }
                    userMessages.show(R.string.error_save_walk)
                }
        }
    }

    private fun LocalDateTime.toEpochMilli(): Long = atZone(clock.zone).toInstant().toEpochMilli()

    private fun LogPastWalkUiState.prefilledFrom(walk: Walk): LogPastWalkUiState {
        val start = walk.startTime.toLocalDateTime()
        val end = checkNotNull(walk.endTime) {
            "walk ${walk.id} has not finished"
        }.toLocalDateTime()
        return copy(
            date = start.toLocalDate(),
            startTime = start.toLocalTime(),
            endTime = end.toLocalTime(),
            selectedHandlers = walk.handlers.toSet(),
            isLoading = false,
        )
    }

    private fun Long.toLocalDateTime(): LocalDateTime =
        Instant.ofEpochMilli(this).atZone(clock.zone).toLocalDateTime()
}
