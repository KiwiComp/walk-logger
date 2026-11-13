package com.example.walklogger.feature.logwalk.finishwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.domain.SaveWalkUseCase
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
    behaviourIssueRepository: BehaviourIssueRepository,
    medicalConcernRepository: MedicalConcernRepository,
    private val walkRepository: WalkRepository,
    private val userMessages: UserMessages,
    clock: Clock,
    fetchWalkWeather: FetchWalkWeatherUseCase,
    locationPermission: LocationPermissionUseCase,
    locationRepository: LocationRepository,
    private val saveWalk: SaveWalkUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(walkId: Long): FinishWalkViewModel
    }

    private val form = WalkFormEditor(
        viewModelScope,
        behaviourIssueRepository,
        medicalConcernRepository,
        userMessages,
        fetchWalkWeather,
        locationPermission,
        locationRepository,
        walkId,
    )

    val formActions: WalkFormActions get() = form

    private val times = WalkTimesEditor(clock, LocalDate.now(clock)) { start, end ->
        form.onTimesChanged(start, end, autoFetch = true)
    }

    private val screen = MutableStateFlow(FinishWalkUiState(times = times.state.value))

    /** The stored walk, kept so saving preserves its id, dog and handlers. */
    private var walk: Walk? = null

    init {
        viewModelScope.launch {
            val stored = walkRepository.observeWalk(walkId).first()
            if (stored == null) {
                screen.update { it.copy(isNotFound = true) }
                userMessages.show(R.string.error_walk_not_found)
                return@launch
            }
            walk = stored
            form.prefillFrom(stored)
            screen.update { it.copy(handlers = stored.handlers, isLoading = false) }
            times.prefillFrom(stored)
        }
    }

    val uiState: StateFlow<FinishWalkUiState> =
        combine(
            screen,
            times.state,
            form.state,
            dogRepository.observeActiveDog(),
        ) { screen, times, form, dog ->
            screen.copy(times = times, form = form, dogName = dog?.name)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = screen.value.copy(form = form.state.value),
        )

    fun toggleEditTimes() = screen.update { it.copy(isEditingTimes = !it.isEditingTimes) }

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
            runCatchingCancellable {
                saveWalk(form.applyTo(stored.copy(startTime = start, endTime = end)))
            }
                .onSuccess { screen.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure {
                    screen.update { it.copy(isSaving = false) }
                    userMessages.show(R.string.error_save_walk)
                }
        }
    }
}
