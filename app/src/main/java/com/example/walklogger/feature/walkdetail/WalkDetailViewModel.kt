package com.example.walklogger.feature.walkdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/** Shows the stored walk [walkId] as it is now, so an edit pushed on top is reflected on return. */
@HiltViewModel(assistedFactory = WalkDetailViewModel.Factory::class)
class WalkDetailViewModel @AssistedInject constructor(
    @Assisted private val walkId: Long,
    private val walkRepository: WalkRepository,
    dogRepository: DogRepository,
    private val clock: Clock,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(walkId: Long): WalkDetailViewModel
    }

    private val discard = MutableStateFlow(WalkDetailUiState())

    /** Once discarded the walk is gone from the repository; only the discard flags remain while the screen leaves. */
    val uiState: StateFlow<WalkDetailUiState> =
        combine(walkRepository.observeWalk(walkId), dogRepository.observeDogs(), discard) { walk, dogs, discard ->
            if (walk == null) {
                discard
            } else {
                discard.detailsOf(walk, dogName = dogs.find { it.id == walk.dogId }?.name)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = discard.value,
        )

    fun discard() {
        if (!uiState.value.canDiscard) return
        discard.update { it.copy(isDiscarding = true) }
        viewModelScope.launch {
            walkRepository.deleteWalk(walkId)
            discard.update { it.copy(isDiscarding = false, isDiscarded = true) }
        }
    }

    private fun WalkDetailUiState.detailsOf(walk: Walk, dogName: String?): WalkDetailUiState {
        val start = walk.startTime.toLocalDateTime()
        return copy(
            isLoading = false,
            date = start.toLocalDate(),
            startTime = start.toLocalTime(),
            endTime = walk.endTime?.toLocalDateTime()?.toLocalTime(),
            dogName = dogName,
            handlers = walk.handlers,
            equipment = walk.equipment,
            rating = walk.rating,
            optionalRatings = walk.optionalRatings,
            rain = walk.rain,
            temperature = walk.temperature,
            wind = walk.wind,
            snow = walk.snow,
            issues = walk.issues,
            concerns = walk.concerns,
            notes = walk.notes,
        )
    }

    private fun Long.toLocalDateTime(): LocalDateTime = Instant.ofEpochMilli(this).atZone(clock.zone).toLocalDateTime()
}
