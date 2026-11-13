package com.example.walklogger.feature.dog.registerdog

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.ui.message.UserMessageState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** With a [dogId] the form is prefilled from that dog and saving updates it instead of inserting. */
@HiltViewModel(assistedFactory = RegisterDogViewModel.Factory::class)
class RegisterDogViewModel @AssistedInject constructor(
    @Assisted private val dogId: Long?,
    private val dogRepository: DogRepository,
    private val walkRepository: WalkRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(dogId: Long?): RegisterDogViewModel
    }

    private val form = MutableStateFlow(
        RegisterDogUiState(isEditing = dogId != null, isLoading = dogId != null),
    )
    private val userMessage = UserMessageState()

    /** Eager so the form's own checks can read [StateFlow.value] while nothing collects. */
    val uiState: StateFlow<RegisterDogUiState> =
        form.combine(userMessage.message) { state, message -> state.copy(userMessage = message) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, form.value)

    init {
        if (dogId != null) {
            viewModelScope.launch {
                val dog = dogRepository.observeDogs().first().find { it.id == dogId }
                if (dog == null) {
                    form.update { it.copy(isNotFound = true) }
                } else {
                    form.update { it.prefilledFrom(dog) }
                }
            }
            viewModelScope.launch {
                walkRepository.observeActiveWalk().collect { walk ->
                    form.update { it.copy(hasActiveWalk = walk?.dogId == dogId) }
                }
            }
        }
    }

    fun setName(name: String) = form.update { it.copy(name = name) }

    fun setBreed(breed: String) = form.update { it.copy(breed = breed) }

    fun setBirthDate(date: LocalDate?) = form.update { it.copy(birthDate = date) }

    fun setWeight(weight: String) = form.update { it.copy(weight = weight) }

    fun save() {
        val state = form.value
        if (!state.canSave) return
        form.update { it.copy(isSaving = true) }
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
                .onSuccess { form.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure {
                    form.update { it.copy(isSaving = false) }
                    userMessage.show(R.string.error_save_dog)
                }
        }
    }

    fun delete() {
        if (dogId == null || !form.value.canDelete) return
        form.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            runCatchingCancellable { dogRepository.deleteDog(dogId) }
                .onSuccess { form.update { it.copy(isDeleting = false, isDeleted = true) } }
                .onFailure {
                    form.update { it.copy(isDeleting = false) }
                    userMessage.show(R.string.error_delete_dog)
                }
        }
    }

    fun userMessageShown(@StringRes message: Int) = userMessage.shown(message)

    private fun RegisterDogUiState.prefilledFrom(dog: Dog) = copy(
        name = dog.name,
        breed = dog.breed,
        birthDate = dog.birthDate,
        // Trailing ".0" would show up in the field as the user's own text and come back on save.
        weight = dog.weightKg?.toString()?.removeSuffix(".0") ?: "",
        isLoading = false,
    )
}
