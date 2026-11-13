package com.example.walklogger.testing

import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.model.Dog
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [DogRepository] for unit tests. */
class FakeDogRepository : DogRepository {

    val failures = FailureSwitch()

    private val dogs = MutableStateFlow<List<Dog>>(emptyList())
    private val activeDogId = MutableStateFlow<Long?>(null)
    private var nextId = 1L

    fun addDogs(vararg newDogs: Dog) = dogs.update { it + newDogs }

    override fun observeDogs(): Flow<List<Dog>> = dogs.map { list ->
        list.filterNot { it.isDeleted }
    }

    override fun observeAllDogs(): Flow<List<Dog>> = dogs

    override fun observeDog(id: Long): Flow<Dog?> = dogs.map { list -> list.find { it.id == id } }

    override fun observeActiveDog(): Flow<Dog?> = combine(dogs, activeDogId) { list, id ->
        list.find { it.id == id }
            ?: list.firstOrNull { !it.isDeleted }
    }

    override suspend fun registerDog(
        name: String,
        breed: String,
        birthDate: LocalDate?,
        weightKg: Double?,
    ): Long {
        failures.check()
        val active = observeActiveDog().first()
        val id = nextId++
        dogs.update {
            it +
                Dog(id = id, name = name, breed = breed, birthDate = birthDate, weightKg = weightKg)
        }
        if (active == null || active.isDeleted) activeDogId.value = id
        return id
    }

    override suspend fun setActiveDog(id: Long) {
        failures.check()
        activeDogId.value = id
    }

    override suspend fun updateDog(
        id: Long,
        name: String,
        breed: String,
        birthDate: LocalDate?,
        weightKg: Double?,
    ) {
        failures.check()
        dogs.update { list ->
            list.map { dog ->
                if (dog.id ==
                    id
                ) {
                    dog.copy(
                        name = name,
                        breed = breed,
                        birthDate = birthDate,
                        weightKg = weightKg,
                    )
                } else {
                    dog
                }
            }
        }
    }

    override suspend fun deleteDog(id: Long) {
        failures.check()
        dogs.update { list -> list.map { if (it.id == id) it.copy(isDeleted = true) else it } }
        if (activeDogId.value == id) activeDogId.value = observeDogs().first().firstOrNull()?.id
    }
}
