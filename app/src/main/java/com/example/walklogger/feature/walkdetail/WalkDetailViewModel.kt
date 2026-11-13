package com.example.walklogger.feature.walkdetail

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.common.toLocalDateTime
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.ui.message.UserMessageState
import com.example.walklogger.domain.DiscardWalkUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val discardWalk: DiscardWalkUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(walkId: Long): WalkDetailViewModel
    }

    private val discard = MutableStateFlow(WalkDetailUiState())
    private val userMessage = UserMessageState()

    init {
        viewModelScope.launch {
            if (walkRepository.observeWalk(walkId).first() == null) {
                discard.update { it.copy(isNotFound = true) }
            }
        }
    }

    /** Once discarded the walk is gone from the repository; only the discard flags remain while the screen leaves. */
    val uiState: StateFlow<WalkDetailUiState> =
        combine(walkRepository.observeWalk(walkId), dogRepository.observeAllDogs(), discard) {
                walk,
                dogs,
                discard,
            ->
            if (walk == null) {
                discard
            } else {
                discard.detailsOf(walk, dog = dogs.find { it.id == walk.dogId })
            }
        }.combine(userMessage.message) { state, message -> state.copy(userMessage = message) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = discard.value,
            )

    fun discard() {
        if (!uiState.value.canDiscard) return
        discard.update { it.copy(isDiscarding = true) }
        viewModelScope.launch {
            runCatchingCancellable { discardWalk(walkId) }
                .onSuccess { discard.update { it.copy(isDiscarding = false, isDiscarded = true) } }
                .onFailure {
                    discard.update { it.copy(isDiscarding = false) }
                    userMessage.show(R.string.error_delete_walk)
                }
        }
    }

    fun userMessageShown(@StringRes message: Int) = userMessage.shown(message)

    private fun WalkDetailUiState.detailsOf(walk: Walk, dog: Dog?): WalkDetailUiState {
        val start = walk.startTime.toLocalDateTime(clock.zone)
        return copy(
            isLoading = false,
            date = start.toLocalDate(),
            startTime = start.toLocalTime(),
            endTime = walk.endTime?.toLocalDateTime(clock.zone)?.toLocalTime(),
            dogName = dog?.name,
            isDogDeleted = dog?.isDeleted == true,
            handlers = walk.handlers,
            equipment = walk.equipment,
            rating = walk.rating,
            optionalRatings = walk.optionalRatings,
            stool = walk.stool,
            weather = walk.weather,
            behaviourIssues = walk.behaviourIssues,
            medicalConcerns = walk.medicalConcerns,
            notes = walk.notes,
        )
    }
}
