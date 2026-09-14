package com.example.walklogger.core.model

import java.time.LocalDate

/** A registered dog. [breed], [birthDate] and [weightKg] are optional profile details. */
data class Dog(
    val id: Long,
    val name: String,
    val breed: String = "",
    val birthDate: LocalDate? = null,
    val weightKg: Double? = null,
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(weightKg == null || weightKg > 0) { "weightKg must be positive, was $weightKg" }
    }
}
