package com.example.walklogger.feature.dog

import androidx.annotation.StringRes

data class DogUiState(
    val isLoading: Boolean = true,
    /** Null when no dog is active: none registered, or only deleted ones. */
    val active: DogProfileUiState? = null,
    /** Every dog that is not deleted, in registration order, including the active one. */
    val dogs: List<DogListItemUiState> = emptyList(),
    val deletedDogs: List<DogListItemUiState> = emptyList(),
    @param:StringRes val userMessage: Int? = null,
)

data class DogProfileUiState(
    val id: Long,
    val name: String,
    val breed: String,
    val ageYears: Int?,
    val weightKg: Double?,
    val isDeleted: Boolean = false,
)

data class DogListItemUiState(val id: Long, val name: String, val isActive: Boolean)
