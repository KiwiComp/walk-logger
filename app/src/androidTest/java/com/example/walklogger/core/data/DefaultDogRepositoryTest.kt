package com.example.walklogger.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.database.WalkLoggerDatabase
import com.example.walklogger.core.datastore.UserPreferencesDataSource
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultDogRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var database: WalkLoggerDatabase
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferences: UserPreferencesDataSource
    private lateinit var repository: DefaultDogRepository

    @Before
    fun setUp() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                WalkLoggerDatabase::class.java,
            ).build()
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStore =
            PreferenceDataStoreFactory.create(scope = dataStoreScope) {
                folder.newFile("prefs.preferences_pb")
            }
        preferences = UserPreferencesDataSource(dataStore)
        repository = DefaultDogRepository(database.dogDao(), preferences, Dispatchers.IO)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        database.close()
    }

    @Test
    fun noDogs_activeDogIsNull() = runBlocking {
        assertNull(repository.observeActiveDog().first())
        assertEquals(emptyList<Any>(), repository.observeDogs().first())
    }

    @Test
    fun registerDog_storesTrimmedDetailsInRegistrationOrder() = runBlocking {
        val birthDate = LocalDate.of(2023, 9, 2)
        val sixtenId = repository.registerDog(
            name = " Sixten ",
            breed = " Border collie ",
            birthDate = birthDate,
            weightKg = 18.0,
        )
        val miraId = repository.registerDog(
            name = "Mira",
            breed = "",
            birthDate = null,
            weightKg = null,
        )

        val dogs = repository.observeDogs().first()
        assertEquals(listOf(sixtenId, miraId), dogs.map { it.id })
        assertEquals("Sixten", dogs[0].name)
        assertEquals("Border collie", dogs[0].breed)
        assertEquals(birthDate, dogs[0].birthDate)
        assertEquals(18.0, dogs[0].weightKg!!, 0.0)
        assertNull(dogs[1].birthDate)
        assertNull(dogs[1].weightKg)
    }

    @Test
    fun firstRegisteredDogBecomesActive_laterOnesDoNot() = runBlocking {
        val sixtenId = repository.registerDog(
            name = "Sixten",
            breed = "",
            birthDate = null,
            weightKg = null,
        )
        repository.registerDog(name = "Mira", breed = "", birthDate = null, weightKg = null)

        assertEquals(sixtenId, repository.observeActiveDog().first()?.id)
        assertEquals(sixtenId, preferences.activeDogId.first())
    }

    @Test
    fun setActiveDog_switchesActiveDog() = runBlocking {
        repository.registerDog(name = "Sixten", breed = "", birthDate = null, weightKg = null)
        val miraId = repository.registerDog(
            name = "Mira",
            breed = "",
            birthDate = null,
            weightKg = null,
        )

        repository.setActiveDog(miraId)

        assertEquals("Mira", repository.observeActiveDog().first()?.name)
    }

    @Test
    fun stalePreference_fallsBackToFirstDog() = runBlocking {
        repository.registerDog(name = "Sixten", breed = "", birthDate = null, weightKg = null)
        repository.registerDog(name = "Mira", breed = "", birthDate = null, weightKg = null)

        preferences.setActiveDogId(99)

        assertEquals("Sixten", repository.observeActiveDog().first()?.name)
    }

    @Test
    fun deleteDog_keepsDogAsDeletedAndHandsOverActive() = runBlocking {
        val sixtenId = repository.registerDog(
            name = "Sixten",
            breed = "",
            birthDate = null,
            weightKg = null,
        )
        val miraId = repository.registerDog(
            name = "Mira",
            breed = "",
            birthDate = null,
            weightKg = null,
        )

        repository.deleteDog(sixtenId)

        assertEquals(listOf(miraId), repository.observeDogs().first().map { it.id })
        assertEquals(listOf(true, false), repository.observeAllDogs().first().map { it.isDeleted })
        assertEquals(miraId, repository.observeActiveDog().first()?.id)
    }

    @Test
    fun deleteLastDog_clearsActiveDog() = runBlocking {
        val sixtenId = repository.registerDog(
            name = "Sixten",
            breed = "",
            birthDate = null,
            weightKg = null,
        )

        repository.deleteDog(sixtenId)

        assertNull(repository.observeActiveDog().first())
        assertNull(preferences.activeDogId.first())
    }

    @Test
    fun deletedDog_canBeSelectedAsActive() = runBlocking {
        val sixtenId = repository.registerDog(
            name = "Sixten",
            breed = "",
            birthDate = null,
            weightKg = null,
        )
        repository.deleteDog(sixtenId)

        repository.setActiveDog(sixtenId)

        val active = repository.observeActiveDog().first()
        assertEquals(sixtenId, active?.id)
        assertEquals(true, active?.isDeleted)
    }

    @Test
    fun registerDog_takesOverFromDeletedActiveDog() = runBlocking {
        val sixtenId = repository.registerDog(
            name = "Sixten",
            breed = "",
            birthDate = null,
            weightKg = null,
        )
        repository.deleteDog(sixtenId)
        repository.setActiveDog(sixtenId)

        val miraId = repository.registerDog(
            name = "Mira",
            breed = "",
            birthDate = null,
            weightKg = null,
        )

        assertEquals(miraId, repository.observeActiveDog().first()?.id)
    }
}
