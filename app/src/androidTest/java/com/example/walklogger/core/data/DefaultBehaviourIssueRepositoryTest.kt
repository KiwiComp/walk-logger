package com.example.walklogger.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.database.DefaultBehaviourIssues
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
class DefaultBehaviourIssueRepositoryTest {

    private lateinit var database: WalkLoggerDatabase
    private lateinit var repository: DefaultBehaviourIssueRepository

    @Before
    fun setUp() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                WalkLoggerDatabase::class.java,
            )
                .addCallback(WalkLoggerDatabase.SeedCallback())
                .build()
        repository = DefaultBehaviourIssueRepository(database.behaviourIssueDao(), Dispatchers.IO)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun aFreshDatabase_startsWithTheKeyedDefaultIssues() = runBlocking {
        val issues = repository.observeBehaviourIssues().first()

        assertEquals(DefaultBehaviourIssues.keys.toList(), issues.map { it.key })
        assertEquals(DefaultBehaviourIssues.values.toList(), issues.map { it.name })
    }

    @Test
    fun addIssue_trimsAndAppendsAfterTheDefaults() = runBlocking {
        val added = repository.addBehaviourIssue("  Digging ")

        assertEquals("Digging", added.name)
        assertNull(added.key)
        assertEquals(added, repository.observeBehaviourIssues().first().last())
    }

    @Test
    fun addIssue_returnsTheExistingIssueIgnoringCase() = runBlocking {
        val existing = repository.observeBehaviourIssues().first().first { it.name == "Pulling" }

        assertEquals(existing, repository.addBehaviourIssue("pulling"))
        assertEquals(10, repository.observeBehaviourIssues().first().size)
    }

    @Test
    fun addIssue_revivesAHiddenIssue() = runBlocking {
        val pulling = repository.observeBehaviourIssues().first().first { it.key == "pulling" }
        database.behaviourIssueDao().setHidden(pulling.id, true)
        assertEquals(9, repository.observeBehaviourIssues().first().size)

        assertEquals(pulling, repository.addBehaviourIssue("pulling"))

        assertEquals(10, repository.observeBehaviourIssues().first().size)
    }

    @Test
    fun addIssue_rejectsBlankNames() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.addBehaviourIssue("   ") }
        }
    }
}
