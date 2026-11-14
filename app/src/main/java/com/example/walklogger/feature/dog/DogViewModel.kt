package com.example.walklogger.feature.dog

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.ui.message.UserMessageState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DogViewModel @Inject constructor(
    private val dogRepository: DogRepository,
    private val clock: Clock,
    errorReporter: ErrorReporter,
) : ViewModel() {

    private val userMessage = UserMessageState(errorReporter)

    val uiState: StateFlow<DogUiState> =
        combine(dogRepository.observeAllDogs(), dogRepository.observeActiveDog()) { dogs, active ->
            val (deleted, remaining) = dogs.partition { it.isDeleted }
            DogUiState(
                isLoading = false,
                active = active?.toProfile(),
                dogs = if (active == null) emptyList() else remaining.map { it.toListItem(active) },
                deletedDogs = deleted.map { it.toListItem(active) },
            )
        }.combine(userMessage.message) { state, message -> state.copy(userMessage = message) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DogUiState(),
            )

    fun switchDog(id: Long) {
        viewModelScope.launch {
            runCatchingCancellable { dogRepository.setActiveDog(id) }
                .onFailure {
                    userMessage.showAndReportError(
                        it,
                        R.string.error_select_dog,
                        "Failed to switch dog",
                    )
                }
        }
    }

    fun userMessageShown(@StringRes message: Int) = userMessage.shown(message)

    private fun Dog.toProfile() = DogProfileUiState(
        id = id,
        name = name,
        breed = breed,
        ageYears = birthDate?.let { Period.between(it, LocalDate.now(clock)).years },
        weightKg = weightKg,
        isDeleted = isDeleted,
    )

    private fun Dog.toListItem(active: Dog?) = DogListItemUiState(
        id = id,
        name = name,
        isActive =
            id == active?.id,
    )
}
