package com.example.walklogger.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.database.WalkLoggerDatabase
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import com.example.walklogger.core.database.entity.MedicalConcernEntity
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.Wind
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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

    private val now = Instant.parse("2026-09-12T07:05:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private val pulling = BehaviourIssue(id = 1, name = "Pulling")
    private val barking = BehaviourIssue(id = 2, name = "Barking")
    private val limping = MedicalConcern(id = 1, name = "Limping")
    private val vomit = MedicalConcern(id = 2, name = "Vomit")

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
        snow = Snow.NONE,
        issues = listOf(pulling, barking),
        concerns = listOf(limping, vomit),
        optionalRatings = mapOf(OptionalRating.REACTIVITY to 2, OptionalRating.DISTRACTION to 6),
        stool = Stool(passed = true, quantity = StoolQuantity.SMALL, texture = StoolTexture.MUSHY, colour = StoolColour.GREEN),
    )

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), WalkLoggerDatabase::class.java).build()
        repository = DefaultWalkRepository(database.walkDao(), clock, Dispatchers.IO)
        listOf(pulling, barking).forEach { database.behaviourIssueDao().insertIssue(BehaviourIssueEntity(id = it.id, name = it.name)) }
        listOf(limping, vomit).forEach { database.medicalConcernDao().insertConcern(MedicalConcernEntity(id = it.id, name = it.name)) }
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
    fun logWalk_keepsAnUnansweredAndANoStoolApart() = runBlocking {
        val unanswered = repository.logWalk(pastWalk.copy(stool = null))
        val no = repository.logWalk(pastWalk.copy(stool = Stool(passed = false)))

        assertNull(repository.observeWalk(unanswered).first()!!.stool)
        assertEquals(Stool(passed = false), repository.observeWalk(no).first()!!.stool)
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
    fun startWalk_thenFinishWalk_stampsBothTimesFromTheClock() = runBlocking {
        val id = repository.startWalk(dogId = 7, handlers = listOf("Anna"), equipment = Equipment.HARNESS)
        val active = repository.observeActiveWalk().first()!!
        assertEquals(id, active.id)
        assertEquals(now.toEpochMilli(), active.startTime)

        repository.finishWalk(id)

        assertNull(repository.observeActiveWalk().first())
        assertEquals(now.toEpochMilli(), repository.observeWalk(id).first()!!.endTime)
    }

    @Test
    fun updateStool_storesAndClearsTheAnswerOfAnActiveWalk() = runBlocking {
        val id = repository.startWalk(dogId = 7, handlers = listOf("Anna"), equipment = Equipment.HARNESS)
        val stool = Stool(passed = true, texture = StoolTexture.SOFT, colour = StoolColour.WHITE_SPECKED)

        repository.updateStool(id, stool)
        assertEquals(stool, repository.observeWalk(id).first()!!.stool)

        repository.updateStool(id, null)
        assertNull(repository.observeWalk(id).first()!!.stool)
    }

    @Test
    fun updateWalk_replacesTheTickedIssues() = runBlocking {
        val id = repository.logWalk(pastWalk)

        repository.updateWalk(pastWalk.copy(id = id, issues = listOf(barking)))

        assertEquals(listOf(barking), repository.observeWalk(id).first()!!.issues)
    }

    @Test
    fun updateWalk_replacesTheTickedConcerns() = runBlocking {
        val id = repository.logWalk(pastWalk)

        repository.updateWalk(pastWalk.copy(id = id, concerns = listOf(vomit)))

        assertEquals(listOf(vomit), repository.observeWalk(id).first()!!.concerns)
    }

    @Test
    fun deleteWalk_removesItAndItsIssueAndConcernRows() = runBlocking {
        val id = repository.logWalk(pastWalk)

        repository.deleteWalk(id)

        assertNull(repository.observeWalk(id).first())
        database.query("SELECT COUNT(*) FROM walk_behaviour_issues", null).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        database.query("SELECT COUNT(*) FROM walk_medical_concerns", null).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }
}
