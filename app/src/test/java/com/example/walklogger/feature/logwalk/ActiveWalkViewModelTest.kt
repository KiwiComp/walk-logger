package com.example.walklogger.feature.logwalk

import app.cash.turbine.test
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Walk
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ActiveWalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneOffset.ofHours(2)
    private val start = LocalDate.of(2026, 9, 12).atTime(7, 5).toInstant(zone)
    private val clock = SteppingClock(start.plus(Duration.ofMinutes(34).plusSeconds(12)), zone)
    private val dogRepository = FakeDogRepository()
    private val walkRepository = FakeWalkRepository()
    private val walk = Walk(
        id = 1,
        dogId = 1,
        handlers = listOf("Anna"),
        startTime = start.toEpochMilli(),
        endTime = null,
        equipment = Equipment.HARNESS,
        notes = "Barked",
    )
    private val viewModel by lazy {
        ActiveWalkViewModel(walkId = 1, dogRepository = dogRepository, walkRepository = walkRepository, clock = clock)
    }

    @Test
    fun `shows the walk, its dog and the time since it started`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Sixten", state.dogName)
            assertEquals(listOf("Anna"), state.handlers)
            assertEquals(Equipment.HARNESS, state.equipment)
            assertEquals(LocalTime.of(7, 5), state.startTime)
            assertEquals(Duration.ofMinutes(34).plusSeconds(12), state.elapsed)
            assertEquals("Barked", state.note)
            assertTrue(state.canEnd)
        }
    }

    @Test
    fun `elapsed time follows the clock once a second`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            assertEquals(Duration.ofMinutes(34).plusSeconds(12), expectMostRecentItem().elapsed)
            clock.advance(Duration.ofSeconds(3))
            advanceTimeBy(3_001)
            assertEquals(Duration.ofMinutes(34).plusSeconds(15), expectMostRecentItem().elapsed)
        }
    }

    @Test
    fun `note is shown at once and stored after a pause in typing`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setNote("Pulled at the gate")
            assertEquals("Pulled at the gate", expectMostRecentItem().note)
            assertEquals("Barked", walkRepository.observeWalk(1).first()!!.notes)
            advanceTimeBy(501)
            assertEquals("Pulled at the gate", walkRepository.observeWalk(1).first()!!.notes)
        }
    }

    @Test
    fun `ending stores the note and the end time`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setNote("Pulled at the gate")
            viewModel.endWalk()
            val state = expectMostRecentItem()
            assertTrue(state.isEnded)
            assertFalse(state.canEnd)
            val stored = walkRepository.observeWalk(1).first()!!
            assertFalse(stored.isActive)
            assertEquals("Pulled at the gate", stored.notes)
        }
    }

    /** A fixed clock the test moves forward by hand. */
    private class SteppingClock(private var now: Instant, private val zone: ZoneId) : Clock() {
        fun advance(by: Duration) {
            now += by
        }

        override fun instant(): Instant = now
        override fun getZone(): ZoneId = zone
        override fun withZone(zone: ZoneId): Clock = SteppingClock(now, zone)
    }
}
