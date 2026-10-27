package com.example.walklogger.feature.dog.registerdog

import java.time.LocalDate

data class RegisterDogUiState(
    val name: String = "",
    val breed: String = "",
    val birthDate: LocalDate? = null,
    /** Raw text so the user can type "18,5" or "18.5"; parsed by [weightKg]. */
    val weight: String = "",
    val isEditing: Boolean = false,
    /** True while the dog being edited is still being read; the form is not editable until it arrives. */
    val isLoading: Boolean = false,
    /** True when the dog no longer exists; the screen closes. */
    val isNotFound: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
) {
    val weightKg: Double? = weight.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

    val isWeightInvalid: Boolean = weight.isNotBlank() && weightKg == null

    val canSave: Boolean =
        name.isNotBlank() && !isWeightInvalid && !isLoading && !isSaving && !isSaved && !isDeleting && !isDeleted

    val canDelete: Boolean = isEditing && !isLoading && !isSaving && !isSaved && !isDeleting && !isDeleted
}
