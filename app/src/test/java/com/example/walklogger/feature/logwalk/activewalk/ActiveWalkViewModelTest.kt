package com.example.walklogger.feature.logwalk.activewalk

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakePlaceRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
import com.example.walklogger.testing.shownMessages
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveWalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneOffset.ofHours(2)
    private val start = LocalDate.of(2026, 9, 12).atTime(7, 5).toInstant(zone)
    private val clock = SteppingClock(start.plus(Duration.ofMinutes(34).plusSeconds(12)), zone)
    private val dogRepository = FakeDogRepository()
    private val walkRepository = FakeWalkRepository()
    private val placeRepository = FakePlaceRepository()
    private val userMessages = UserMessages()
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
        ActiveWalkViewModel(
            walkId = 1,
            dogRepository = dogRepository,
            walkRepository = walkRepository,
            placeRepository = placeRepository,
            userMessages = userMessages,
            clock = clock,
        )
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

    @Test
    fun `discarding deletes the walk and its stored position and reports it`() = runTest {
        walkRepository.addWalks(walk)
        placeRepository.walkLocation.value = WalkLocation(walkId = 1, Coordinates(59.33, 18.07))
        viewModel.uiState.test {
            viewModel.discardWalk()
            val state = expectMostRecentItem()
            assertTrue(state.isDiscarded)
            assertFalse(state.canEnd)
            assertFalse(state.canDiscard)
            assertNull(walkRepository.observeWalk(1).first())
            assertTrue(walkRepository.observeWalks().first().isEmpty())
            assertNull(placeRepository.walkLocation.value)
        }
    }

    @Test
    fun `a note typed just before discarding is not written afterwards`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setNote("Pulled at the gate")
            viewModel.discardWalk()
            advanceTimeBy(1_000)
            assertTrue(walkRepository.observeWalks().first().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stool switch is off until turned on, which stores a yes and opens the description`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            val off = expectMostRecentItem()
            assertNull(off.stool)
            assertFalse(off.isStoolExpanded)
            viewModel.setStoolPassed(true)
            val on = expectMostRecentItem()
            assertEquals(Stool(passed = true), on.stool)
            assertTrue(on.isStoolExpanded)
            assertEquals(Stool(passed = true), walkRepository.observeWalk(1).first()!!.stool)
        }
    }

    @Test
    fun `stool picks are stored at once`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setStoolPassed(true)
            viewModel.selectStoolQuantity(StoolQuantity.LARGE)
            viewModel.selectStoolTexture(StoolTexture.SOFT)
            viewModel.selectStoolColour(StoolColour.WHITE_SPECKED)
            val expected = Stool(passed = true, quantity = StoolQuantity.LARGE, texture = StoolTexture.SOFT, colour = StoolColour.WHITE_SPECKED)
            assertEquals(expected, expectMostRecentItem().stool)
            assertEquals(expected, walkRepository.observeWalk(1).first()!!.stool)
        }
    }

    @Test
    fun `stool description only applies once the switch is on`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.selectStoolTexture(StoolTexture.SOFT)
            assertNull(expectMostRecentItem().stool)
            assertNull(walkRepository.observeWalk(1).first()!!.stool)
        }
    }

    @Test
    fun `turning the stool switch off stores null, dropping the description`() = runTest {
        walkRepository.addWalks(walk.copy(stool = Stool(passed = true, colour = StoolColour.GREEN)))
        viewModel.uiState.test {
            expectMostRecentItem()
            viewModel.setStoolPassed(false)
            assertNull(expectMostRecentItem().stool)
            assertNull(walkRepository.observeWalk(1).first()!!.stool)
            viewModel.setStoolPassed(true)
            assertEquals(Stool(passed = true), expectMostRecentItem().stool)
        }
    }

    @Test
    fun `done folds the description without changing the answer, and it opens again`() = runTest {
        val stool = Stool(passed = true, quantity = StoolQuantity.NORMAL)
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setStoolPassed(true)
            viewModel.selectStoolQuantity(StoolQuantity.NORMAL)
            viewModel.foldStool()
            val folded = expectMostRecentItem()
            assertFalse(folded.isStoolExpanded)
            assertEquals(stool, folded.stool)
            assertEquals(stool, walkRepository.observeWalk(1).first()!!.stool)
            viewModel.expandStool()
            assertTrue(expectMostRecentItem().isStoolExpanded)
        }
    }

    @Test
    fun `a stored stool shows folded`() = runTest {
        val stool = Stool(passed = true, colour = StoolColour.GREEN)
        walkRepository.addWalks(walk.copy(stool = stool))
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(stool, state.stool)
            assertFalse(state.isStoolExpanded)
        }
    }

    @Test
    fun `ending keeps the stool`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setStoolPassed(true)
            viewModel.endWalk()
            assertTrue(expectMostRecentItem().isEnded)
            assertEquals(Stool(passed = true), walkRepository.observeWalk(1).first()!!.stool)
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

    @Test
    fun `a failed end keeps the timer running and says so`() = runTest {
        walkRepository.addWalks(walk)
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.endWalk()
            val state = expectMostRecentItem()
            assertFalse(state.isEnding)
            assertFalse(state.isEnded)
            assertTrue(state.canEnd)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(R.string.error_end_walk), shown)
    }

    @Test
    fun `a failed discard keeps the walk and its note keeps saving`() = runTest {
        walkRepository.addWalks(walk)
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.discardWalk()
            assertTrue(expectMostRecentItem().canDiscard)
            walkRepository.failures.failWrites = false
            viewModel.setNote("Pulled at the gate")
            advanceTimeBy(1_000)
            assertEquals("Pulled at the gate", walkRepository.observeWalk(1).first()!!.notes)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(R.string.error_delete_walk), shown)
    }

    @Test
    fun `a failed note save says so and later notes are still stored`() = runTest {
        walkRepository.addWalks(walk)
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.setNote("Pulled")
            advanceTimeBy(1_000)
            walkRepository.failures.failWrites = false
            viewModel.setNote("Pulled at the gate")
            advanceTimeBy(1_000)
            assertEquals("Pulled at the gate", walkRepository.observeWalk(1).first()!!.notes)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(R.string.error_save_note), shown)
    }

    @Test
    fun `a failed stool save says so`() = runTest {
        walkRepository.addWalks(walk)
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            walkRepository.failures.failWrites = true
            viewModel.setStoolPassed(true)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(R.string.error_save_stool), shown)
    }

    @Test
    fun `a missing walk closes the screen and says so`() = runTest {
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().isNotFound)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(R.string.error_walk_not_found), shown)
    }
}
