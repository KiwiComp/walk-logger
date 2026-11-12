package com.example.walklogger.feature.logwalk.pastwalk

import app.cash.turbine.test
import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.feature.logwalk.SaveBlocker
import com.example.walklogger.testing.FakeBehaviourIssueRepository
import com.example.walklogger.testing.FakeDogRepository
import com.example.walklogger.testing.FakeHandlerRepository
import com.example.walklogger.testing.FakeLocationProvider
import com.example.walklogger.testing.FakeMedicalConcernRepository
import com.example.walklogger.testing.FakePlaceRepository
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.FakeWeatherRepository
import com.example.walklogger.testing.MainDispatcherRule
import com.example.walklogger.testing.shownMessages
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

class LogPastWalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 9, 12)
    private val zone = ZoneOffset.ofHours(2)
    private val now = date.atTime(20, 0)
    private val clock = Clock.fixed(now.toInstant(zone), zone)
    private val dogRepository = FakeDogRepository()
    private val handlerRepository = FakeHandlerRepository()
    private val behaviourIssueRepository = FakeBehaviourIssueRepository()
    private val medicalConcernRepository = FakeMedicalConcernRepository()
    private val walkRepository = FakeWalkRepository()
    private val userMessages = UserMessages()
    private val locationProvider = FakeLocationProvider()
    private val placeRepository = FakePlaceRepository()
    private val weatherRepository = FakeWeatherRepository()
    private var walkId: Long? = null
    private val viewModel by lazy {
        LogPastWalkViewModel(
            date = date,
            walkId = walkId,
            dogRepository = dogRepository,
            handlerRepository = handlerRepository,
            behaviourIssueRepository = behaviourIssueRepository,
            medicalConcernRepository = medicalConcernRepository,
            walkRepository = walkRepository,
            userMessages = userMessages,
            clock = clock,
            fetchWalkWeather = FetchWalkWeatherUseCase(
                locationProvider,
                placeRepository,
                weatherRepository,
                clock,
            ),
            locationPermission = LocationPermissionUseCase(locationProvider, placeRepository),
            placeRepository = placeRepository,
        )
    }

    /** Relies on the first stored handler being preselected. */
    private fun fillInValidWalk() {
        viewModel.setStartTime(LocalTime.of(17, 40))
        viewModel.setEndTime(LocalTime.of(18, 5))
        viewModel.formActions.setRating(5)
    }

    @Test
    fun `date is prefilled and times are empty`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(date, state.date)
            assertNull(state.startTime)
            assertNull(state.endTime)
            assertNull(state.durationMinutes)
            assertNull(state.form.stool)
        }
    }

    @Test
    fun `shows the active dog, stored handlers, issues and concerns`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Anna", "Jonas")
        behaviourIssueRepository.addBehaviourIssues("Pulling", "Barking")
        medicalConcernRepository.addMedicalConcerns("Limping", "Vomit")
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Sixten", state.dogName)
            assertEquals(listOf("Anna", "Jonas"), state.handlers)
            assertEquals(setOf("Anna"), state.selectedHandlers)
            assertEquals(
                listOf(BehaviourIssue(1, "Pulling"), BehaviourIssue(2, "Barking")),
                state.form.behaviourIssues,
            )
            assertEquals(
                listOf(MedicalConcern(1, "Limping"), MedicalConcern(2, "Vomit")),
                state.form.medicalConcerns,
            )
        }
    }

    @Test
    fun `duration follows the entered times`() = runTest {
        viewModel.uiState.test {
            viewModel.setStartTime(LocalTime.of(17, 40))
            viewModel.setEndTime(LocalTime.of(18, 5))
            assertEquals(25, expectMostRecentItem().durationMinutes)
        }
    }

    @Test
    fun `save is blocked by the first missing input in form order`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            viewModel.toggleHandler("Anna")
            assertEquals(SaveBlocker.HANDLER, expectMostRecentItem().saveBlocker)
            viewModel.toggleHandler("Anna")
            assertEquals(SaveBlocker.TIMES, expectMostRecentItem().saveBlocker)
            viewModel.setStartTime(LocalTime.of(18, 5))
            viewModel.setEndTime(LocalTime.of(17, 40))
            assertEquals(SaveBlocker.END_BEFORE_START, expectMostRecentItem().saveBlocker)
            viewModel.setEndTime(LocalTime.of(18, 30))
            assertEquals(SaveBlocker.RATING, expectMostRecentItem().saveBlocker)
            viewModel.formActions.setRating(4)
            val state = expectMostRecentItem()
            assertNull(state.saveBlocker)
            assertTrue(state.canSave)
        }
    }

    @Test
    fun `date picker is capped at today`() = runTest {
        viewModel.uiState.test {
            assertEquals(date, expectMostRecentItem().maxDate)
        }
    }

    @Test
    fun `a walk ending after now is blocked`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            fillInValidWalk()
            viewModel.setEndTime(now.toLocalTime().plusMinutes(1))
            assertEquals(SaveBlocker.IN_FUTURE, expectMostRecentItem().saveBlocker)
            viewModel.setEndTime(now.toLocalTime())
            assertNull(expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `a walk on a future day is blocked`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            fillInValidWalk()
            viewModel.setDate(date.plusDays(1))
            assertEquals(SaveBlocker.IN_FUTURE, expectMostRecentItem().saveBlocker)
            viewModel.setDate(date.minusDays(1))
            assertNull(expectMostRecentItem().saveBlocker)
        }
    }

    @Test
    fun `adding a handler stores and selects it`() = runTest {
        viewModel.uiState.test {
            viewModel.addHandler("  Elin ")
            val state = expectMostRecentItem()
            assertEquals(listOf("Elin"), state.handlers)
            assertEquals(setOf("Elin"), state.selectedHandlers)
        }
    }

    @Test
    fun `save stores the walk for the active dog and flags completion`() = runTest {
        dogRepository.addDogs(Dog(id = 7, name = "Sixten"))
        handlerRepository.addHandlers("Anna", "Jonas")
        behaviourIssueRepository.addBehaviourIssues("Pulling", "Barking")
        medicalConcernRepository.addMedicalConcerns("Limping", "Vomit")
        viewModel.uiState.test {
            fillInValidWalk()
            viewModel.formActions.toggleBehaviourIssue(BehaviourIssue(2, "Barking"))
            viewModel.formActions.toggleMedicalConcern(MedicalConcern(2, "Vomit"))
            viewModel.formActions.selectEquipment(Equipment.COLLAR)
            viewModel.formActions.setTemperature("-3")
            viewModel.formActions.selectCondition(WeatherCondition.SNOW)
            viewModel.formActions.setOptionalRating(OptionalRating.ENERGY_LEVEL, 6)
            viewModel.formActions.selectStoolPassed(false)
            viewModel.formActions.setNotes(" Pulled at the gate ")
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        val walk = walkRepository.observeWalks().first().single()
        assertEquals(7, walk.dogId)
        assertEquals(listOf("Anna"), walk.handlers)
        assertEquals(date.atTime(17, 40).toInstant(zone).toEpochMilli(), walk.startTime)
        assertEquals(date.atTime(18, 5).toInstant(zone).toEpochMilli(), walk.endTime)
        assertEquals(5, walk.rating)
        assertEquals(Equipment.COLLAR, walk.equipment)
        assertEquals(
            weatherRepository.weather.copy(
                temperatureCelsius = -3.0,
                condition = WeatherCondition.SNOW,
            ),
            walk.weather,
        )
        assertEquals(mapOf(OptionalRating.ENERGY_LEVEL to 6), walk.optionalRatings)
        assertEquals(Stool(passed = false), walk.stool)
        assertEquals(listOf(BehaviourIssue(2, "Barking")), walk.behaviourIssues)
        assertEquals(listOf(MedicalConcern(2, "Vomit")), walk.medicalConcerns)
        assertEquals("Pulled at the gate", walk.notes)
    }

    @Test
    fun `save uses the date picked in the form`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Anna")
        val picked = date.minusDays(3)
        viewModel.uiState.test {
            fillInValidWalk()
            viewModel.setDate(picked)
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        val walk = walkRepository.observeWalks().first().single()
        assertEquals(picked.atTime(17, 40).toInstant(zone).toEpochMilli(), walk.startTime)
    }

    private fun seedLoggedWalk(): Walk {
        val walk = Walk(
            id = 42,
            dogId = 7,
            handlers = listOf("Jonas"),
            startTime = date.minusDays(2).atTime(8, 15).toInstant(zone).toEpochMilli(),
            endTime = date.minusDays(2).atTime(8, 50).toInstant(zone).toEpochMilli(),
            notes = "Muddy",
            equipment = Equipment.COLLAR,
            rating = 3,
            weather = WalkWeather(
                condition = WeatherCondition.HEAVY_RAIN,
                temperatureCelsius = 4.0,
                rainMm = 3.1,
            ),
            behaviourIssues = listOf(BehaviourIssue(1, "Pulling")),
            medicalConcerns = listOf(MedicalConcern(1, "Limping")),
            optionalRatings = mapOf(OptionalRating.RESPONSIVENESS to 4),
            stool = Stool(
                passed = true,
                quantity = StoolQuantity.NORMAL,
                colour = StoolColour.DARK_BROWN,
            ),
        )
        walkRepository.addWalks(walk)
        walkId = walk.id
        return walk
    }

    @Test
    fun `editing prefills the form from the stored walk`() = runTest {
        seedLoggedWalk()
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state.isEditing)
            assertFalse(state.isLoading)
            assertEquals(date.minusDays(2), state.date)
            assertEquals(LocalTime.of(8, 15), state.startTime)
            assertEquals(LocalTime.of(8, 50), state.endTime)
            assertEquals(setOf("Jonas"), state.selectedHandlers)
            assertEquals(3, state.form.rating)
            assertEquals("Muddy", state.form.notes)
            assertTrue(state.canSave)
        }
    }

    @Test
    fun `a new walk fetches the weather once both times are set`() = runTest {
        handlerRepository.addHandlers("Anna")
        viewModel.uiState.test {
            viewModel.setStartTime(LocalTime.of(17, 40))
            assertTrue(weatherRepository.requests.isEmpty())
            viewModel.setEndTime(LocalTime.of(18, 5))
            assertEquals(weatherRepository.weather, expectMostRecentItem().form.fetchedWeather)
        }
        assertEquals(1, weatherRepository.requests.size)
    }

    @Test
    fun `editing keeps the stored weather and fetches only on request`() = runTest {
        val original = seedLoggedWalk()
        viewModel.uiState.test {
            assertEquals(original.weather, expectMostRecentItem().form.weather)
            viewModel.setStartTime(LocalTime.of(8, 0))
            expectMostRecentItem()
            assertTrue(weatherRepository.requests.isEmpty())
            viewModel.formActions.fetchWeather()
            assertEquals(weatherRepository.weather, expectMostRecentItem().form.fetchedWeather)
        }
    }

    @Test
    fun `saving an edit updates the walk and keeps its id and dog`() = runTest {
        val original = seedLoggedWalk()
        dogRepository.addDogs(Dog(id = 1, name = "Other"))
        handlerRepository.addHandlers("Anna", "Jonas")
        viewModel.uiState.test {
            expectMostRecentItem()
            viewModel.formActions.setRating(6)
            viewModel.formActions.setNotes("Dry after all")
            viewModel.toggleHandler("Anna")
            viewModel.save()
            assertTrue(expectMostRecentItem().isSaved)
        }
        val walk = walkRepository.observeWalks().first().single()
        assertEquals(original.id, walk.id)
        assertEquals(original.dogId, walk.dogId)
        assertEquals(original.startTime, walk.startTime)
        assertEquals(6, walk.rating)
        assertEquals("Dry after all", walk.notes)
        assertEquals(listOf("Anna", "Jonas"), walk.handlers)
    }

    @Test
    fun `save does nothing while the form is incomplete`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        viewModel.uiState.test {
            viewModel.save()
            assertFalse(expectMostRecentItem().isSaved)
        }
        assertTrue(walkRepository.observeWalks().first().isEmpty())
    }

    @Test
    fun `a failed save keeps the form open and says so`() = runTest {
        dogRepository.addDogs(Dog(id = 1, name = "Sixten"))
        handlerRepository.addHandlers("Kim")
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            fillInValidWalk()
            walkRepository.failures.failWrites = true
            viewModel.save()
            val state = expectMostRecentItem()
            assertFalse(state.isSaving)
            assertFalse(state.isSaved)
            assertTrue(state.canSave)
        }
        assertEquals(listOf(R.string.error_save_walk), shown)
    }

    @Test
    fun `editing a missing walk closes the screen and says so`() = runTest {
        walkId = 9
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().isNotFound)
        }
        assertEquals(listOf(R.string.error_walk_not_found), shown)
    }

    @Test
    fun `a failed handler add says so and selects nothing new`() = runTest {
        val shown = shownMessages(userMessages)
        viewModel.uiState.test {
            handlerRepository.failures.failWrites = true
            viewModel.addHandler("Anna")
            assertTrue(expectMostRecentItem().selectedHandlers.isEmpty())
        }
        assertEquals(listOf(R.string.error_add_name), shown)
    }
}
