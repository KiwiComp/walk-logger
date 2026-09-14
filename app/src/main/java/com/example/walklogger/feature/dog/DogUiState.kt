package com.example.walklogger.feature.dog

sealed interface DogUiState {
    data object Loading : DogUiState

    data object NoDog : DogUiState

    data class Profile(
        val active: DogProfileUiState,
        /** Every registered dog in registration order, including the active one. */
        val dogs: List<DogListItemUiState>,
    ) : DogUiState
}

data class DogProfileUiState(
    val name: String,
    val breed: String,
    val ageYears: Int?,
    val weightKg: Double?,
)

data class DogListItemUiState(
    val id: Long,
    val name: String,
    val isActive: Boolean,
)
