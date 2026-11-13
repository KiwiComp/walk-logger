package com.example.walklogger.domain

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeWalkRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveWalkUseCaseTest {

    private val walkRepository = FakeWalkRepository()
    private val activeWalkLocationRepository = FakeActiveWalkLocationRepository()
    private val saveWalk = SaveWalkUseCase(walkRepository, activeWalkLocationRepository)
    private val stockholm = Coordinates(59.33, 18.07)

    private fun walk(id: Long, dogId: Long = 3, notes: String = "") = Walk(
        id = id,
        dogId = dogId,
        handlers = listOf("Anna"),
        startTime = 1_000,
        endTime = 2_000,
        notes = notes,
    )

    @Test
    fun `an unsaved walk is logged and leaves the start position alone`() = runTest {
        activeWalkLocationRepository.walkLocation.value = WalkLocation(9, stockholm)
        saveWalk(walk(id = SaveWalkUseCase.UNSAVED, dogId = 7))
        assertEquals(7L, walkRepository.observeWalks().first().single().dogId)
        assertEquals(WalkLocation(9, stockholm), activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `a failed save throws`() = runTest {
        walkRepository.failures.failWrites = true
        assertTrue(runCatching { saveWalk(walk(id = SaveWalkUseCase.UNSAVED)) }.isFailure)
    }

    @Test
    fun `a stored walk is replaced and its start position cleared`() = runTest {
        walkRepository.addWalks(walk(id = 4))
        activeWalkLocationRepository.walkLocation.value = WalkLocation(4, stockholm)
        saveWalk(walk(id = 4, notes = "Calm"))
        assertEquals("Calm", walkRepository.observeWalk(4).first()!!.notes)
        assertNull(activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `another walk's start position is kept`() = runTest {
        walkRepository.addWalks(walk(id = 4))
        activeWalkLocationRepository.walkLocation.value = WalkLocation(9, stockholm)
        saveWalk(walk(id = 4))
        assertEquals(WalkLocation(9, stockholm), activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `a position that cannot be cleared does not fail the save`() = runTest {
        walkRepository.addWalks(walk(id = 4))
        activeWalkLocationRepository.walkLocation.value = WalkLocation(4, stockholm)
        activeWalkLocationRepository.failures.failWrites = true
        saveWalk(walk(id = 4, notes = "Calm"))
        assertEquals("Calm", walkRepository.observeWalk(4).first()!!.notes)
    }
}
