package com.example.walklogger.core.model

/**
 * Something medical noticed on a walk ("Limping", "Vomit", …). Walks reference concerns by [id],
 * so renaming one keeps every walk it was logged on and patterns can be counted per concern.
 * Seeded defaults carry a stable [key] the UI localises; user-added concerns show [name] as typed.
 */
data class MedicalConcern(val id: Long, val name: String, val key: String? = null) {
    init {
        require(name.isNotBlank()) { "concern name must not be blank" }
    }
}
