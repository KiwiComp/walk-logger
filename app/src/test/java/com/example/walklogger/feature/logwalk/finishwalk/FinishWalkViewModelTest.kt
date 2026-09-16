package com.example.walklogger.feature.logwalk.finishwalk

import app.cash.turbine.test
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.Wind
import com.example.walklogger.feature.logwalk.SaveBlocker
import com.example.walklogger.testing.FakeBehaviourIssueRepository
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeMedicalConcernRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


class FinishWalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 9, 12)
    private val zone = ZoneOffset.ofHours(2)
    private val now = date.atTime(7, 39)
    private val clock = Clock.fixed(now.toInstant(zone), zone)
    private val dogRepository = FakeDogRepository()
    private val behaviourIssueRepository = FakeBehaviourIssueRepository()
    private val medicalConcernRepository = FakeMedicalConcernRepository()
    private val walkRepository = FakeWalkRepository()
    private val walk = Walk(
        id = 1,
        dogId = 1,
        handlers = listOf("Anna"),
        startTime = date.atTime(7, 5).toInstant(zone).toEpochMilli(),
        endTime = now.toInstant(zone).toEpochMilli(),
        equipment = Equipment.HARNESS,
        notes = "Barked",
    )
    private val viewModel by lazy {
        FinishWalkViewModel(
            walkId = 1,
            dogRepository = dogRepository,
            behaviourIssueRepository = behaviourIssueRepository,
            medicalConcernRepository = medicalConcernRepository,
            walkRepository = walkRepository,
            clock = clock,
        )
    }

    @Test
    fun `prefills the recorded times, handlers and note`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals("Sixten", state.dogName)
            assertEquals(date, state.date)
            assertEquals(LocalTime.of(7, 5), state.startTime)
            assertEquals(LocalTime.of(7, 39), state.endTime)
            assertEquals(34, state.durationMinutes)
            assertEquals(listOf("Anna"), state.handlers)
            assertEquals(Equipment.HARNESS, state.equipment)
            assertEquals("Barked", state.notes)
            assertEquals(SaveBlocker.RATING, state.saveBlocker)
        }
    }

    @Test
    fun `edit times toggles the pickers`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().isEditingTimes)
            viewModel.toggleEditTimes()
            assertTrue(expectMostRecentItem().isEditingTimes)
            viewModel.toggleEditTimes()
            assertFalse(expectMostRecentItem().isEditingTimes)
        }
    }

    @Test
    fun `weather starts collapsed for a walk without conditions and toggles`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().isWeatherExpanded)
            viewModel.toggleWeather()
            assertTrue(expectMostRecentItem().isWeatherExpanded)
            viewModel.toggleWeather()
            assertFalse(expectMostRecentItem().isWeatherExpanded)
        }
    }

    @Test
    fun `weather starts expanded for a walk with conditions`() = runTest {
        walkRepository.addWalks(walk.copy(snow = Snow.HEAVY))
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(Snow.HEAVY, state.snow)
            assertTrue(state.isWeatherExpanded)
        }
    }

    @Test
    fun `clear weather resets every condition`() = runTest {
        walkRepository.addWalks(walk.copy(rain = Rain.HEAVY, temperature = Temperature.HOT))
        viewModel.uiState.test {
            viewModel.selectWind(Wind.STILL)
            viewModel.selectSnow(Snow.LIGHT)
            viewModel.clearWeather()
            val state = expectMostRecentItem()
            assertNull(state.rain)
            assertNull(state.temperature)
            assertNull(state.wind)
            assertNull(state.snow)
        }
    }

    @Test
    fun `behaviour starts collapsed for a walk without issues and toggles`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().isBehaviourExpanded)
            viewModel.toggleBehaviour()
            assertTrue(expectMostRecentItem().isBehaviourExpanded)
        }
    }

    @Test
    fun `behaviour starts expanded for a walk with issues`() = runTest {
        walkRepository.addWalks(walk.copy(issues = listOf(BehaviourIssue(3, "Lunging"))))
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(setOf(3L), state.selectedIssueIds)
            assertTrue(state.isBehaviourExpanded)
        }
    }

    @Test
    fun `adding an issue stores and selects it, clear issues deselects all`() = runTest {
        walkRepository.addWalks(walk)
        behaviourIssueRepository.addIssues("Pulling")
        viewModel.uiState.test {
            viewModel.toggleIssue(BehaviourIssue(1, "Pulling"))
            viewModel.addIssue("Digging")
            val state = expectMostRecentItem()
            assertEquals(listOf(BehaviourIssue(1, "Pulling"), BehaviourIssue(2, "Digging")), state.issues)
            assertEquals(setOf(1L, 2L), state.selectedIssueIds)
            viewModel.clearIssues()
            assertTrue(expectMostRecentItem().selectedIssueIds.isEmpty())
        }
    }

    @Test
    fun `medical starts collapsed for a walk without concerns and toggles`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().isMedicalExpanded)
            viewModel.toggleMedical()
            assertTrue(expectMostRecentItem().isMedicalExpanded)
        }
    }

    @Test
    fun `medical starts expanded for a walk with concerns`() = runTest {
        walkRepository.addWalks(walk.copy(concerns = listOf(MedicalConcern(4, "Limping"))))
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(setOf(4L), state.selectedConcernIds)
            assertTrue(state.isMedicalExpanded)
        }
    }

    @Test
    fun `adding a concern stores and selects it, clear concerns deselects all`() = runTest {
        walkRepository.addWalks(walk)
        medicalConcernRepository.addConcerns("Limping")
        viewModel.uiState.test {
            viewModel.toggleConcern(MedicalConcern(1, "Limping"))
            viewModel.addConcern("Coughing")
            val state = expectMostRecentItem()
            assertEquals(listOf(MedicalConcern(1, "Limping"), MedicalConcern(2, "Coughing")), state.concerns)
            assertEquals(setOf(1L, 2L), state.selectedConcernIds)
            viewModel.clearConcerns()
            assertTrue(expectMostRecentItem().selectedConcernIds.isEmpty())
        }
    }

    @Test
    fun `end before start blocks saving`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setRating(6)
            viewModel.setEndTime(LocalTime.of(7, 0))
            assertEquals(SaveBlocker.END_BEFORE_START, expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `an end in the future blocks saving`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.setRating(6)
            viewModel.setEndTime(LocalTime.of(8, 0))
            assertEquals(SaveBlocker.IN_FUTURE, expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `saving updates the stored walk and keeps its id, dog and handlers`() = runTest {
        walkRepository.addWalks(walk)
        behaviourIssueRepository.addIssues("Pulling", "Barking")
        medicalConcernRepository.addConcerns("Limping", "Vomit")
        viewModel.uiState.test {
            viewModel.setStartTime(LocalTime.of(7, 0))
            viewModel.toggleIssue(BehaviourIssue(2, "Barking"))
            viewModel.toggleConcern(MedicalConcern(1, "Limping"))
            viewModel.setRating(6)
            viewModel.selectEquipment(Equipment.COLLAR)
            viewModel.selectRain(Rain.NONE)
            viewModel.selectTemperature(Temperature.COOL)
            viewModel.selectWind(Wind.BREEZE)
            viewModel.selectSnow(Snow.LIGHT)
            viewModel.setNotes("  Off-lead in the field  ")
            viewModel.save()

            assertTrue(expectMostRecentItem().isSaved)
            val stored = walkRepository.observeWalk(1).first()!!
            assertEquals(1L, stored.id)
            assertEquals(1L, stored.dogId)
            assertEquals(listOf("Anna"), stored.handlers)
            assertEquals(date.atTime(7, 0).toInstant(zone).toEpochMilli(), stored.startTime)
            assertEquals(walk.endTime, stored.endTime)
            assertEquals(6, stored.rating)
            assertEquals(Equipment.COLLAR, stored.equipment)
            assertEquals(Rain.NONE, stored.rain)
            assertEquals(Temperature.COOL, stored.temperature)
            assertEquals(Wind.BREEZE, stored.wind)
            assertEquals(Snow.LIGHT, stored.snow)
            assertEquals(listOf(BehaviourIssue(2, "Barking")), stored.issues)
            assertEquals(listOf(MedicalConcern(1, "Limping")), stored.concerns)
            assertEquals("Off-lead in the field", stored.notes)
        }
    }

    @Test
    fun `saving without a rating does nothing`() = runTest {
        walkRepository.addWalks(walk)
        viewModel.uiState.test {
            viewModel.save()
            assertFalse(expectMostRecentItem().isSaved)
            assertNull(walkRepository.observeWalk(1).first()!!.rating)
        }
    }
}
