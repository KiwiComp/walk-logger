package com.example.walklogger.feature.logwalk.finishwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.Wind
import com.example.walklogger.feature.logwalk.MAX_NOTES_LENGTH
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



/** Rates and describes the walk with [walkId], which the timer screen has already finished. */
@HiltViewModel(assistedFactory = FinishWalkViewModel.Factory::class)
class FinishWalkViewModel @AssistedInject constructor(
    @Assisted private val walkId: Long,
    dogRepository: DogRepository,
    private val behaviourIssueRepository: BehaviourIssueRepository,
    private val medicalConcernRepository: MedicalConcernRepository,
    private val walkRepository: WalkRepository,
    private val clock: Clock,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(walkId: Long): FinishWalkViewModel
    }

    private val form = MutableStateFlow(FinishWalkUiState(now = LocalDateTime.now(clock)))

    /** The stored walk, kept so saving preserves its id, dog and handlers. */
    private var walk: Walk? = null

    init {
        viewModelScope.launch {
            val stored = checkNotNull(walkRepository.observeWalk(walkId).first()) { "walk $walkId not found" }
            walk = stored
            form.update { it.prefilledFrom(stored) }
        }
    }

    val uiState: StateFlow<FinishWalkUiState> =
        combine(
            form,
            dogRepository.observeActiveDog(),
            behaviourIssueRepository.observeIssues(),
            medicalConcernRepository.observeConcerns(),
        ) { form, dog, issues, concerns ->
            form.copy(dogName = dog?.name, issues = issues, concerns = concerns)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = form.value,
        )

    fun toggleEditTimes() = form.update { it.copy(isEditingTimes = !it.isEditingTimes) }

    fun setDate(date: LocalDate) = updateWhen { it.copy(date = date) }

    fun setStartTime(time: LocalTime) = updateWhen { it.copy(startTime = time) }

    fun setEndTime(time: LocalTime) = updateWhen { it.copy(endTime = time) }

    /** Re-reads the clock alongside the edit so a form left open still validates against the present. */
    private fun updateWhen(edit: (FinishWalkUiState) -> FinishWalkUiState) =
        form.update { edit(it).copy(now = LocalDateTime.now(clock)) }

    fun setRating(rating: Int) = form.update { it.copy(rating = rating) }

    fun selectEquipment(value: Equipment) = form.update { it.copy(equipment = value) }

    fun selectRain(value: Rain) = form.update { it.copy(rain = value) }

    fun selectTemperature(value: Temperature) = form.update { it.copy(temperature = value) }

    fun selectWind(value: Wind) = form.update { it.copy(wind = value) }

    fun selectSnow(value: Snow) = form.update { it.copy(snow = value) }

    fun toggleWeather() = form.update { it.copy(isWeatherExpanded = !it.isWeatherExpanded) }

    fun clearWeather() = form.update { it.copy(rain = null, temperature = null, wind = null, snow = null) }

    fun toggleIssue(issue: BehaviourIssue) = form.update { state ->
        val selected = state.selectedIssueIds
        state.copy(selectedIssueIds = if (issue.id in selected) selected - issue.id else selected + issue.id)
    }

    /** Stores the issue (or finds the existing one) and selects it. */
    fun addIssue(name: String) {
        viewModelScope.launch {
            val stored = behaviourIssueRepository.addIssue(name)
            form.update { it.copy(selectedIssueIds = it.selectedIssueIds + stored.id) }
        }
    }

    fun toggleBehaviour() = form.update { it.copy(isBehaviourExpanded = !it.isBehaviourExpanded) }

    fun clearIssues() = form.update { it.copy(selectedIssueIds = emptySet()) }

    fun toggleConcern(concern: MedicalConcern) = form.update { state ->
        val selected = state.selectedConcernIds
        state.copy(selectedConcernIds = if (concern.id in selected) selected - concern.id else selected + concern.id)
    }

    /** Stores the concern (or finds the existing one) and selects it. */
    fun addConcern(name: String) {
        viewModelScope.launch {
            val stored = medicalConcernRepository.addConcern(name)
            form.update { it.copy(selectedConcernIds = it.selectedConcernIds + stored.id) }
        }
    }

    fun toggleMedical() = form.update { it.copy(isMedicalExpanded = !it.isMedicalExpanded) }

    fun clearConcerns() = form.update { it.copy(selectedConcernIds = emptySet()) }

    fun setNotes(notes: String) = form.update { it.copy(notes = notes.take(MAX_NOTES_LENGTH)) }

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        val stored = checkNotNull(walk) { "walk $walkId has not loaded" }
        form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            walkRepository.updateWalk(
                stored.copy(
                    startTime = state.date.atTime(state.startTime!!).toEpochMilli(),
                    endTime = state.date.atTime(state.endTime!!).toEpochMilli(),
                    notes = state.notes.trim(),
                    equipment = state.equipment,
                    rating = state.rating,
                    rain = state.rain,
                    temperature = state.temperature,
                    wind = state.wind,
                    snow = state.snow,
                    issues = state.issues.filter { it.id in state.selectedIssueIds },
                    concerns = state.concerns.filter { it.id in state.selectedConcernIds },
                ),
            )
            form.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun LocalDateTime.toEpochMilli(): Long = atZone(clock.zone).toInstant().toEpochMilli()

    private fun FinishWalkUiState.prefilledFrom(walk: Walk): FinishWalkUiState {
        val start = walk.startTime.toLocalDateTime()
        val end = checkNotNull(walk.endTime) { "walk ${walk.id} has not finished" }.toLocalDateTime()
        return copy(
            date = start.toLocalDate(),
            startTime = start.toLocalTime(),
            endTime = end.toLocalTime(),
            handlers = walk.handlers,
            rating = walk.rating,
            equipment = walk.equipment ?: equipment,
            rain = walk.rain,
            temperature = walk.temperature,
            wind = walk.wind,
            snow = walk.snow,
            isWeatherExpanded = listOf(walk.rain, walk.temperature, walk.wind, walk.snow).any { it != null },
            selectedIssueIds = walk.issues.map { it.id }.toSet(),
            isBehaviourExpanded = walk.issues.isNotEmpty(),
            selectedConcernIds = walk.concerns.map { it.id }.toSet(),
            isMedicalExpanded = walk.concerns.isNotEmpty(),
            notes = walk.notes,
            isLoading = false,
        )
    }

    private fun Long.toLocalDateTime(): LocalDateTime = Instant.ofEpochMilli(this).atZone(clock.zone).toLocalDateTime()
}
