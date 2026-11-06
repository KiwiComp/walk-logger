package com.example.walklogger.core.model

import java.time.LocalDate

/**
 * A registered dog. [breed], [birthDate] and [weightKg] are optional profile details.
 * A deleted dog keeps its walks and can still be viewed, but no new walks are logged for it.
 */
data class Dog(
    val id: Long,
    val name: String,
    val breed: String = "",
    val birthDate: LocalDate? = null,
    val weightKg: Double? = null,
    val isDeleted: Boolean = false,
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(weightKg == null || weightKg > 0) { "weightKg must be positive, was $weightKg" }
    }
}
