package com.example.walklogger.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.database.WalkLoggerDatabase
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.Wind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultWalkRepositoryTest {

    private lateinit var database: WalkLoggerDatabase
    private lateinit var repository: DefaultWalkRepository

    private val pastWalk = Walk(
        id = 99,
        dogId = 7,
        handlers = listOf("Anna", "Jonas"),
        startTime = 1_000,
        endTime = 2_500,
        notes = "Pulled at the gate",
        equipment = Equipment.COLLAR,
        rating = 5,
        rain = Rain.LIGHT,
        temperature = Temperature.COLD,
        wind = Wind.WINDY,
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), WalkLoggerDatabase::class.java).build()
        repository = DefaultWalkRepository(database.walkDao(), Dispatchers.IO)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun logWalk_storesEveryFieldAndAssignsItsOwnId() = runBlocking {
        val id = repository.logWalk(pastWalk)

        val stored = repository.observeWalk(id).first()!!
        assertEquals(pastWalk.copy(id = id), stored)
        assertEquals(id, repository.observeWalks().first().single().id)
    }

    @Test
    fun logWalk_ignoresTheGivenId() = runBlocking {
        val first = repository.logWalk(pastWalk)
        val second = repository.logWalk(pastWalk)

        assertTrue(first != second)
        assertEquals(2, repository.observeWalks().first().size)
    }

    @Test
    fun logWalk_rejectsAWalkWithoutEndTime() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.logWalk(pastWalk.copy(endTime = null)) }
        }
    }

    @Test
    fun loggedWalk_isNotTheActiveWalk() = runBlocking {
        repository.logWalk(pastWalk)

        assertNull(repository.observeActiveWalk().first())
    }

    @Test
    fun startWalk_thenFinishWalk_setsEndTime() = runBlocking {
        val id = repository.startWalk(dogId = 7, handlers = listOf("Anna"), equipment = Equipment.HARNESS)
        assertEquals(id, repository.observeActiveWalk().first()?.id)

        repository.finishWalk(id)

        assertNull(repository.observeActiveWalk().first())
        assertTrue(repository.observeWalk(id).first()!!.endTime != null)
    }

    @Test
    fun deleteWalk_removesIt() = runBlocking {
        val id = repository.logWalk(pastWalk)

        repository.deleteWalk(id)

        assertNull(repository.observeWalk(id).first())
    }
}
