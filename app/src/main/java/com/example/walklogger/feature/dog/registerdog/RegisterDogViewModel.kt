package com.example.walklogger.feature.dog.registerdog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.ui.message.UserMessages
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** With a [dogId] the form is prefilled from that dog and saving updates it instead of inserting. */
@HiltViewModel(assistedFactory = RegisterDogViewModel.Factory::class)
class RegisterDogViewModel @AssistedInject constructor(
    @Assisted private val dogId: Long?,
    private val dogRepository: DogRepository,
    private val walkRepository: WalkRepository,
    private val userMessages: UserMessages,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(dogId: Long?): RegisterDogViewModel
    }

    private val _uiState = MutableStateFlow(
        RegisterDogUiState(isEditing = dogId != null, isLoading = dogId != null),
    )
    val uiState: StateFlow<RegisterDogUiState> = _uiState.asStateFlow()

    init {
        if (dogId != null) {
            viewModelScope.launch {
                val dog = dogRepository.observeDogs().first().find { it.id == dogId }
                if (dog == null) {
                    _uiState.update { it.copy(isNotFound = true) }
                    userMessages.show(R.string.error_dog_not_found)
                } else {
                    _uiState.update { it.prefilledFrom(dog) }
                }
            }
            viewModelScope.launch {
                walkRepository.observeActiveWalk().collect { walk ->
                    _uiState.update { it.copy(hasActiveWalk = walk?.dogId == dogId) }
                }
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name) }

    fun setBreed(breed: String) = _uiState.update { it.copy(breed = breed) }

    fun setBirthDate(date: LocalDate?) = _uiState.update { it.copy(birthDate = date) }

    fun setWeight(weight: String) = _uiState.update { it.copy(weight = weight) }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatchingCancellable {
                if (dogId == null) {
                    dogRepository.registerDog(
                        name = state.name.trim(),
                        breed = state.breed.trim(),
                        birthDate = state.birthDate,
                        weightKg = state.weightKg,
                    )
                } else {
                    dogRepository.updateDog(
                        id = dogId,
                        name = state.name.trim(),
                        breed = state.breed.trim(),
                        birthDate = state.birthDate,
                        weightKg = state.weightKg,
                    )
                }
            }
                .onSuccess { _uiState.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false) }
                    userMessages.show(R.string.error_save_dog)
                }
        }
    }

    fun delete() {
        if (dogId == null || !_uiState.value.canDelete) return
        _uiState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            runCatchingCancellable { dogRepository.deleteDog(dogId) }
                .onSuccess { _uiState.update { it.copy(isDeleting = false, isDeleted = true) } }
                .onFailure {
                    _uiState.update { it.copy(isDeleting = false) }
                    userMessages.show(R.string.error_delete_dog)
                }
        }
    }

    private fun RegisterDogUiState.prefilledFrom(dog: Dog) = copy(
        name = dog.name,
        breed = dog.breed,
        birthDate = dog.birthDate,
        // Trailing ".0" would show up in the field as the user's own text and come back on save.
        weight = dog.weightKg?.toString()?.removeSuffix(".0") ?: "",
        isLoading = false,
    )
}
