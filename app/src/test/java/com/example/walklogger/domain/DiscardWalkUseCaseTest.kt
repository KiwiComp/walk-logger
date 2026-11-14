package com.example.walklogger.domain

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeErrorReporter
import com.example.walklogger.testing.FakeWalkRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscardWalkUseCaseTest {

    private val walkRepository = FakeWalkRepository()
    private val activeWalkLocationRepository = FakeActiveWalkLocationRepository()
    private val errorReporter = FakeErrorReporter()
    private val discardWalk =
        DiscardWalkUseCase(walkRepository, activeWalkLocationRepository, errorReporter)
    private val stockholm = Coordinates(59.33, 18.07)
    private val walk =
        Walk(id = 4, dogId = 3, handlers = listOf("Anna"), startTime = 1_000, endTime = null)

    @Test
    fun `deletes the walk and its start position`() = runTest {
        walkRepository.addWalks(walk)
        activeWalkLocationRepository.walkLocation.value = WalkLocation(4, stockholm)
        discardWalk(4)
        assertNull(walkRepository.observeWalk(4).first())
        assertNull(activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `keeps another walk's start position`() = runTest {
        walkRepository.addWalks(walk)
        activeWalkLocationRepository.walkLocation.value = WalkLocation(9, stockholm)
        discardWalk(4)
        assertEquals(WalkLocation(9, stockholm), activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `a failed delete throws and keeps the position`() = runTest {
        walkRepository.addWalks(walk)
        activeWalkLocationRepository.walkLocation.value = WalkLocation(4, stockholm)
        walkRepository.failures.failWrites = true
        assertTrue(runCatching { discardWalk(4) }.isFailure)
        assertEquals(WalkLocation(4, stockholm), activeWalkLocationRepository.walkLocation.value)
    }

    @Test
    fun `a position that cannot be cleared is reported and does not fail the discard`() = runTest {
        walkRepository.addWalks(walk)
        activeWalkLocationRepository.failures.failWrites = true
        discardWalk(4)
        assertNull(walkRepository.observeWalk(4).first())
        assertEquals(
            listOf("Failed to clear walk location"),
            errorReporter.reported.map {
                it.second
            },
        )
    }
}
