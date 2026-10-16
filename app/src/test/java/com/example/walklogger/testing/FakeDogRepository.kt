package com.example.walklogger.testing

import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.model.Dog
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/** In-memory [DogRepository] for unit tests. */
class FakeDogRepository : DogRepository {

    private val dogs = MutableStateFlow<List<Dog>>(emptyList())
    private val activeDogId = MutableStateFlow<Long?>(null)
    private var nextId = 1L

    fun addDogs(vararg newDogs: Dog) = dogs.update { it + newDogs }

    override fun observeDogs(): Flow<List<Dog>> = dogs

    override fun observeActiveDog(): Flow<Dog?> =
        combine(dogs, activeDogId) { list, id -> list.find { it.id == id } ?: list.firstOrNull() }

    override suspend fun registerDog(name: String, breed: String, birthDate: LocalDate?, weightKg: Double?): Long {
        val id = nextId++
        dogs.update { it + Dog(id = id, name = name, breed = breed, birthDate = birthDate, weightKg = weightKg) }
        if (activeDogId.value == null) activeDogId.value = id
        return id
    }

    override suspend fun setActiveDog(id: Long) {
        activeDogId.value = id
    }

    override suspend fun updateDog(id: Long, name: String, breed: String, birthDate: LocalDate?, weightKg: Double?) {
        dogs.update { list ->
            list.map { dog ->
                if (dog.id == id) dog.copy(name = name, breed = breed, birthDate = birthDate, weightKg = weightKg) else dog
            }
        }
    }

    override suspend fun deleteDog(id: Long) = dogs.update { list -> list.filterNot { it.id == id } }
}
