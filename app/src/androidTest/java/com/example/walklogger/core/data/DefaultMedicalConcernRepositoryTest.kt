package com.example.walklogger.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.database.DefaultMedicalConcerns
import com.example.walklogger.core.database.WalkLoggerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultMedicalConcernRepositoryTest {

    private lateinit var database: WalkLoggerDatabase
    private lateinit var repository: DefaultMedicalConcernRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), WalkLoggerDatabase::class.java)
            .addCallback(WalkLoggerDatabase.SeedCallback())
            .build()
        repository = DefaultMedicalConcernRepository(database.medicalConcernDao(), Dispatchers.IO)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun aFreshDatabase_startsWithTheKeyedDefaultConcerns() = runBlocking {
        val concerns = repository.observeMedicalConcerns().first()

        assertEquals(DefaultMedicalConcerns.keys.toList(), concerns.map { it.key })
        assertEquals(DefaultMedicalConcerns.values.toList(), concerns.map { it.name })
    }

    @Test
    fun addConcern_trimsAndAppendsAfterTheDefaults() = runBlocking {
        val added = repository.addMedicalConcern("  Coughing ")

        assertEquals("Coughing", added.name)
        assertNull(added.key)
        assertEquals(added, repository.observeMedicalConcerns().first().last())
    }

    @Test
    fun addConcern_returnsTheExistingConcernIgnoringCase() = runBlocking {
        val existing = repository.observeMedicalConcerns().first().first { it.name == "Limping" }

        assertEquals(existing, repository.addMedicalConcern("limping"))
        assertEquals(8, repository.observeMedicalConcerns().first().size)
    }

    @Test
    fun addConcern_revivesAHiddenConcern() = runBlocking {
        val limping = repository.observeMedicalConcerns().first().first { it.key == "limping" }
        database.medicalConcernDao().setHidden(limping.id, true)
        assertEquals(7, repository.observeMedicalConcerns().first().size)

        assertEquals(limping, repository.addMedicalConcern("limping"))

        assertEquals(8, repository.observeMedicalConcerns().first().size)
    }

    @Test
    fun addConcern_rejectsBlankNames() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.addMedicalConcern("   ") }
        }
    }
}
