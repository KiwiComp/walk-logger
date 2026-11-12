package com.example.walklogger.core.model

import java.time.LocalDate
import org.junit.Assert.assertThrows
import org.junit.Test

class DogTest {

    @Test
    fun `only a name is required`() {
        Dog(id = 1, name = "Mira")
        Dog(
            id = 1,
            name = "Mira",
            breed = "Border collie",
            birthDate = LocalDate.of(2023, 9, 2),
            weightKg = 18.5,
        )
    }

    @Test
    fun `blank name is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { Dog(id = 1, name = "") }
        assertThrows(IllegalArgumentException::class.java) { Dog(id = 1, name = "   ") }
    }

    @Test
    fun `weight must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            Dog(id = 1, name = "Mira", weightKg = 0.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Dog(id = 1, name = "Mira", weightKg = -1.0)
        }
    }
}
