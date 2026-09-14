package com.example.walklogger.feature.dog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterDogViewModel @Inject constructor(
    private val dogRepository: DogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterDogUiState())
    val uiState: StateFlow<RegisterDogUiState> = _uiState.asStateFlow()

    fun setName(name: String) = _uiState.update { it.copy(name = name) }

    fun setBreed(breed: String) = _uiState.update { it.copy(breed = breed) }

    fun setBirthDate(date: LocalDate?) = _uiState.update { it.copy(birthDate = date) }

    fun setWeight(weight: String) = _uiState.update { it.copy(weight = weight) }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            dogRepository.registerDog(
                name = state.name.trim(),
                breed = state.breed.trim(),
                birthDate = state.birthDate,
                weightKg = state.weightKg,
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
