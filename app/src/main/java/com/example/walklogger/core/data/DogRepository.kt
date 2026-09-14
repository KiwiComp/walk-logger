package com.example.walklogger.core.data

import com.example.walklogger.core.model.Dog
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** Single source of truth for registered dogs and which one is active. */
interface DogRepository {
    /** All dogs in registration order. */
    fun observeDogs(): Flow<List<Dog>>

    /** The dog walks are logged for; null until a dog has been registered. */
    fun observeActiveDog(): Flow<Dog?>

    /** Registers a dog and returns its id. The first dog registered becomes active. */
    suspend fun registerDog(name: String, breed: String, birthDate: LocalDate?, weightKg: Double?): Long

    suspend fun setActiveDog(id: Long)
}
