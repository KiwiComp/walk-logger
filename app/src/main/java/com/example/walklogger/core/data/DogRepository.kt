package com.example.walklogger.core.data

import com.example.walklogger.core.model.Dog
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** Single source of truth for registered dogs and which one is active. */
interface DogRepository {
    /** Dogs that are not deleted, in registration order. */
    fun observeDogs(): Flow<List<Dog>>

    /** Every dog in registration order, including deleted ones. */
    fun observeAllDogs(): Flow<List<Dog>>

    /** The dog with [id], deleted or not; null when there is none. */
    fun observeDog(id: Long): Flow<Dog?>

    /** The dog whose walks are shown; null when no dog is left. A deleted dog is active only if explicitly selected. */
    fun observeActiveDog(): Flow<Dog?>

    /** Registers a dog and returns its id. It becomes active if no dog is active or the active one is deleted. */
    suspend fun registerDog(
        name: String,
        breed: String,
        birthDate: LocalDate?,
        weightKg: Double?,
    ): Long

    suspend fun setActiveDog(id: Long)

    suspend fun updateDog(
        id: Long,
        name: String,
        breed: String,
        birthDate: LocalDate?,
        weightKg: Double?,
    )

    /** Keeps the dog and its walks but marks it deleted. An active dog hands over to the first remaining dog. */
    suspend fun deleteDog(id: Long)
}
