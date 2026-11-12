package com.example.walklogger.feature.dog

sealed interface DogUiState {
    data object Loading : DogUiState

    data class NoDog(val deletedDogs: List<DogListItemUiState> = emptyList()) : DogUiState

    data class Profile(
        val active: DogProfileUiState,
        /** Every dog that is not deleted, in registration order, including the active one. */
        val dogs: List<DogListItemUiState>,
        val deletedDogs: List<DogListItemUiState> = emptyList(),
    ) : DogUiState
}

data class DogProfileUiState(
    val id: Long,
    val name: String,
    val breed: String,
    val ageYears: Int?,
    val weightKg: Double?,
    val isDeleted: Boolean = false,
)

data class DogListItemUiState(val id: Long, val name: String, val isActive: Boolean)
