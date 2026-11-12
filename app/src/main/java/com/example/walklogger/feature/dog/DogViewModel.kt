package com.example.walklogger.feature.dog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.ui.message.UserMessages
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
    private val userMessages: UserMessages,
    private val clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<DogUiState> =
        combine(dogRepository.observeAllDogs(), dogRepository.observeActiveDog()) { dogs, active ->
            val (deleted, remaining) = dogs.partition { it.isDeleted }
            if (active == null) {
                DogUiState.NoDog(deletedDogs = deleted.map { it.toListItem(active) })
            } else {
                buildProfile(active, remaining, deleted)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DogUiState.Loading,
        )

    fun switchDog(id: Long) {
        viewModelScope.launch {
            runCatchingCancellable { dogRepository.setActiveDog(id) }
                .onFailure { userMessages.show(R.string.error_select_dog) }
        }
    }

    private fun buildProfile(active: Dog, remaining: List<Dog>, deleted: List<Dog>) =
        DogUiState.Profile(
            active = DogProfileUiState(
                id = active.id,
                name = active.name,
                breed = active.breed,
                ageYears = active.birthDate?.let { Period.between(it, LocalDate.now(clock)).years },
                weightKg = active.weightKg,
                isDeleted = active.isDeleted,
            ),
            dogs = remaining.map { it.toListItem(active) },
            deletedDogs = deleted.map { it.toListItem(active) },
        )

    private fun Dog.toListItem(active: Dog?) = DogListItemUiState(
        id = id,
        name = name,
        isActive =
            id == active?.id,
    )
}
