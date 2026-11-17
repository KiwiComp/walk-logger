package com.example.walklogger.feature.dog

import androidx.annotation.StringRes

data class DogUiState(
    val isLoading: Boolean = true,
    /** Every dog that is not deleted, in registration order; empty while no dog is active. */
    val dogs: List<DogListItemUiState> = emptyList(),
    val deletedDogs: List<DogListItemUiState> = emptyList(),
    @param:StringRes val userMessage: Int? = null,
)

data class DogListItemUiState(
    val id: Long,
    val name: String,
    val isActive: Boolean,
    val breed: String = "",
    val ageYears: Int? = null,
    val weightKg: Double? = null,
)
