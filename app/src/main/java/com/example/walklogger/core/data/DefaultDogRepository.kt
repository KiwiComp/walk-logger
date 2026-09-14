package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.database.DogDao
import com.example.walklogger.core.database.entity.DogEntity
import com.example.walklogger.core.datastore.UserPreferencesDataSource
import com.example.walklogger.core.model.Dog
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Room-backed [DogRepository]; the active dog id lives in DataStore. */
@Singleton
class DefaultDogRepository @Inject constructor(
    private val dogDao: DogDao,
    private val preferences: UserPreferencesDataSource,
    @param:Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : DogRepository {

    override fun observeDogs(): Flow<List<Dog>> =
        dogDao.observeDogs().map { dogs -> dogs.map { it.toModel() } }

    // Falls back to the first dog so a stale or missing preference never hides a registered dog.
    override fun observeActiveDog(): Flow<Dog?> =
        combine(observeDogs(), preferences.activeDogId) { dogs, activeId ->
            dogs.find { it.id == activeId } ?: dogs.firstOrNull()
        }

    override suspend fun registerDog(
        name: String,
        breed: String,
        birthDate: LocalDate?,
        weightKg: Double?,
    ): Long = withContext(ioDispatcher) {
        val id = dogDao.insertDog(
            DogEntity(
                name = name.trim(),
                breed = breed.trim(),
                birthDate = birthDate?.toEpochDay(),
                weightKg = weightKg,
            ),
        )
        if (preferences.activeDogId.first() == null) preferences.setActiveDogId(id)
        id
    }

    override suspend fun setActiveDog(id: Long) = preferences.setActiveDogId(id)
}
