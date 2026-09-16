package com.example.walklogger.feature.logwalk.pastwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.HandlerRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Equipment
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
    private val walkRepository: WalkRepository,
    private val clock: Clock,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(date: LocalDate, walkId: Long?): LogPastWalkViewModel
    }

    /** Everything the user enters; the dog and handler list are overlaid from their repositories. */
    private val form = MutableStateFlow(
        LogPastWalkUiState(
            date = date,
            now = LocalDateTime.now(clock),
            isEditing = walkId != null,
            isLoading = walkId != null,
        ),
    )

    /** The walk being edited, kept so saving preserves its id and dog. */
    private var editedWalk: Walk? = null

    init {
        if (walkId != null) {
            viewModelScope.launch {
                val walk = checkNotNull(walkRepository.observeWalk(walkId).first()) { "walk $walkId not found" }
                editedWalk = walk
                form.update { it.prefilledFrom(walk) }
            }
        }
    }

    val uiState: StateFlow<LogPastWalkUiState> =
        combine(
            form,
            dogRepository.observeActiveDog(),
            handlerRepository.observeHandlers(),
        ) { form, dog, handlers ->
            form.copy(dogName = dog?.name, handlers = handlers)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = form.value,
        )

    fun setDate(date: LocalDate) = updateWhen { it.copy(date = date) }

    fun setStartTime(time: LocalTime) = updateWhen { it.copy(startTime = time) }

    fun setEndTime(time: LocalTime) = updateWhen { it.copy(endTime = time) }

    /** Re-reads the clock alongside the edit so a form left open still validates against the present. */
    private fun updateWhen(edit: (LogPastWalkUiState) -> LogPastWalkUiState) =
        form.update { edit(it).copy(now = LocalDateTime.now(clock)) }

    fun toggleHandler(handler: String) = form.update { state ->
        val selected = state.selectedHandlers
        state.copy(selectedHandlers = if (handler in selected) selected - handler else selected + handler)
    }

    /** Stores the name (or finds the existing one) and selects it. */
    fun addHandler(name: String) {
        viewModelScope.launch {
            val stored = handlerRepository.addHandler(name)
            form.update { it.copy(selectedHandlers = it.selectedHandlers + stored) }
        }
    }

    fun setRating(rating: Int) = form.update { it.copy(rating = rating) }

    fun selectEquipment(value: Equipment) = form.update { it.copy(equipment = value) }

    fun selectRain(value: Rain) = form.update { it.copy(rain = value) }

    fun selectTemperature(value: Temperature) = form.update { it.copy(temperature = value) }

    fun selectWind(value: Wind) = form.update { it.copy(wind = value) }

    fun selectSnow(value: Snow) = form.update { it.copy(snow = value) }

    fun toggleWeather() = form.update { it.copy(isWeatherExpanded = !it.isWeatherExpanded) }

    fun clearWeather() = form.update { it.copy(rain = null, temperature = null, wind = null, snow = null) }

    fun setNotes(notes: String) = form.update { it.copy(notes = notes.take(MAX_NOTES_LENGTH)) }

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val edited = editedWalk
            val dogId = edited?.dogId
                ?: checkNotNull(dogRepository.observeActiveDog().first()) { "log past walk needs an active dog" }.id
            val walk = Walk(
                id = edited?.id ?: 0,
                dogId = dogId,
                handlers = state.handlers.filter { it in state.selectedHandlers },
                startTime = state.date.atTime(state.startTime!!).toEpochMilli(),
                endTime = state.date.atTime(state.endTime!!).toEpochMilli(),
                notes = state.notes.trim(),
                equipment = state.equipment,
                rating = state.rating,
                rain = state.rain,
                temperature = state.temperature,
                wind = state.wind,
                snow = state.snow,
            )
            if (edited != null) walkRepository.updateWalk(walk) else walkRepository.logWalk(walk)
            form.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun delete() {
        if (!uiState.value.canDelete) return
        form.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            walkRepository.deleteWalk(checkNotNull(walkId))
            form.update { it.copy(isDeleting = false, isDeleted = true) }
        }
    }

    private fun LocalDateTime.toEpochMilli(): Long = atZone(clock.zone).toInstant().toEpochMilli()

    private fun LogPastWalkUiState.prefilledFrom(walk: Walk): LogPastWalkUiState {
        val start = walk.startTime.toLocalDateTime()
        val end = checkNotNull(walk.endTime) { "walk ${walk.id} has not finished" }.toLocalDateTime()
        return copy(
            date = start.toLocalDate(),
            startTime = start.toLocalTime(),
            endTime = end.toLocalTime(),
            selectedHandlers = walk.handlers.toSet(),
            rating = walk.rating,
            equipment = walk.equipment ?: equipment,
            rain = walk.rain,
            temperature = walk.temperature,
            wind = walk.wind,
            snow = walk.snow,
            isWeatherExpanded = listOf(walk.rain, walk.temperature, walk.wind, walk.snow).any { it != null },
            notes = walk.notes,
            isLoading = false,
        )
    }

    private fun Long.toLocalDateTime(): LocalDateTime = Instant.ofEpochMilli(this).atZone(clock.zone).toLocalDateTime()
}
