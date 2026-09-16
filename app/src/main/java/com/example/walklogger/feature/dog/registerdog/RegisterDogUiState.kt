package com.example.walklogger.feature.dog.registerdog

import java.time.LocalDate

data class RegisterDogUiState(
    val name: String = "",
    val breed: String = "",
    val birthDate: LocalDate? = null,
    /** Raw text so the user can type "18,5" or "18.5"; parsed by [weightKg]. */
    val weight: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    val weightKg: Double? = weight.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

    val isWeightInvalid: Boolean = weight.isNotBlank() && weightKg == null

    val canSave: Boolean = name.isNotBlank() && !isWeightInvalid && !isSaving && !isSaved
}
