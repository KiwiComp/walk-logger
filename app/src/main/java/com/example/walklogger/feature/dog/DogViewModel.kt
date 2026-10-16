package com.example.walklogger.feature.dog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.model.Dog
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
) : ViewModel() {

    val uiState: StateFlow<DogUiState> =
        combine(dogRepository.observeDogs(), dogRepository.observeActiveDog()) { dogs, active ->
            if (active == null) DogUiState.NoDog else buildProfile(active, dogs)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DogUiState.Loading,
        )

    fun switchDog(id: Long) {
        viewModelScope.launch { dogRepository.setActiveDog(id) }
    }

    private fun buildProfile(active: Dog, dogs: List<Dog>) = DogUiState.Profile(
        active = DogProfileUiState(
            id = active.id,
            name = active.name,
            breed = active.breed,
            ageYears = active.birthDate?.let { Period.between(it, LocalDate.now(clock)).years },
            weightKg = active.weightKg,
        ),
        dogs = dogs.map { DogListItemUiState(id = it.id, name = it.name, isActive = it.id == active.id) },
    )
}
