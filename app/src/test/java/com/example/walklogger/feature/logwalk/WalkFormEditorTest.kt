package com.example.walklogger.feature.logwalk

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
import com.example.walklogger.testing.FakeBehaviourIssueRepository
import com.example.walklogger.testing.FakeMedicalConcernRepository
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkFormEditorTest {

    private val behaviourIssueRepository = FakeBehaviourIssueRepository()
    private val medicalConcernRepository = FakeMedicalConcernRepository()
    private val walk = Walk(id = 42, dogId = 7, handlers = listOf("Jonas"), startTime = 1_000, endTime = 2_000)

    private fun runEditorTest(body: suspend TestScope.(WalkFormEditor) -> Unit) = runTest(UnconfinedTestDispatcher()) {
        body(WalkFormEditor(backgroundScope, behaviourIssueRepository, medicalConcernRepository))
    }

    @Test
    fun `starts blank with collar and the stored issues and concerns`() {
        behaviourIssueRepository.addIssues("Pulling")
        medicalConcernRepository.addConcerns("Limping")
        runEditorTest { editor ->
            val form = editor.state.value
            assertNull(form.rating)
            assertNull(form.stool)
            assertEquals(Equipment.COLLAR, form.equipment)
            assertEquals(listOf(BehaviourIssue(1, "Pulling")), form.behaviourIssues)
            assertEquals(listOf(MedicalConcern(1, "Limping")), form.medicalConcerns)
            assertFalse(form.isOptionalRatingsExpanded)
            assertFalse(form.isWeatherExpanded)
            assertFalse(form.isBehaviourIssuesExpanded)
            assertFalse(form.isMedicalConcernsExpanded)
        }
    }

    @Test
    fun `optional sections toggle open and closed`() = runEditorTest { editor ->
        editor.toggleOptionalRatings()
        editor.toggleWeather()
        editor.toggleBehaviourIssuesExpanded()
        editor.toggleMedicalConcernsExpanded()
        with(editor.state.value) {
            assertTrue(isOptionalRatingsExpanded)
            assertTrue(isWeatherExpanded)
            assertTrue(isBehaviourIssuesExpanded)
            assertTrue(isMedicalConcernsExpanded)
        }
        editor.toggleOptionalRatings()
        editor.toggleWeather()
        editor.toggleBehaviourIssuesExpanded()
        editor.toggleMedicalConcernsExpanded()
        with(editor.state.value) {
            assertFalse(isOptionalRatingsExpanded)
            assertFalse(isWeatherExpanded)
            assertFalse(isBehaviourIssuesExpanded)
            assertFalse(isMedicalConcernsExpanded)
        }
    }

    @Test
    fun `an optional rating can be set, replaced, removed and cleared`() = runEditorTest { editor ->
        editor.setOptionalRating(OptionalRating.REACTIVITY, 3)
        editor.setOptionalRating(OptionalRating.DISTRACTION, 7)
        assertEquals(mapOf(OptionalRating.REACTIVITY to 3, OptionalRating.DISTRACTION to 7), editor.state.value.optionalRatings)
        editor.setOptionalRating(OptionalRating.REACTIVITY, 5)
        editor.setOptionalRating(OptionalRating.DISTRACTION, null)
        assertEquals(mapOf(OptionalRating.REACTIVITY to 5), editor.state.value.optionalRatings)
        editor.clearOptionalRatings()
        assertTrue(editor.state.value.optionalRatings.isEmpty())
    }

    @Test
    fun `stool description only applies after yes, and no drops it`() = runEditorTest { editor ->
        editor.selectStoolQuantity(StoolQuantity.SMALL)
        assertNull(editor.state.value.stool)
        editor.selectStoolPassed(true)
        editor.selectStoolQuantity(StoolQuantity.LARGE)
        editor.selectStoolTexture(StoolTexture.SOFT)
        editor.selectStoolColour(StoolColour.GREY)
        assertEquals(Stool(true, StoolQuantity.LARGE, StoolTexture.SOFT, StoolColour.GREY), editor.state.value.stool)
        editor.selectStoolColour(null)
        editor.selectStoolPassed(true)
        assertEquals(Stool(true, StoolQuantity.LARGE, StoolTexture.SOFT), editor.state.value.stool)
        editor.selectStoolPassed(false)
        assertEquals(Stool(passed = false), editor.state.value.stool)
        editor.clearStool()
        assertNull(editor.state.value.stool)
    }

    @Test
    fun `yes opens the stool description, done folds it and edit opens it again`() = runEditorTest { editor ->
        editor.selectStoolPassed(true)
        assertTrue(editor.state.value.isStoolExpanded)
        editor.selectStoolQuantity(StoolQuantity.NORMAL)
        editor.foldStool()
        assertFalse(editor.state.value.isStoolExpanded)
        assertEquals(Stool(passed = true, quantity = StoolQuantity.NORMAL), editor.state.value.stool)
        editor.selectStoolPassed(true)
        assertFalse(editor.state.value.isStoolExpanded)
        editor.expandStool()
        assertTrue(editor.state.value.isStoolExpanded)
    }

    @Test
    fun `clear weather resets every condition`() = runEditorTest { editor ->
        editor.selectRain(Rain.LIGHT)
        editor.selectTemperature(Temperature.COOL)
        editor.selectWind(Wind.BREEZE)
        editor.selectSnow(Snow.HEAVY)
        editor.clearWeather()
        with(editor.state.value) {
            assertNull(rain)
            assertNull(temperature)
            assertNull(wind)
            assertNull(snow)
        }
    }

    @Test
    fun `issues toggle on and off, clear together, and an added one is stored and selected`() {
        behaviourIssueRepository.addIssues("Pulling", "Barking")
        runEditorTest { editor ->
            editor.toggleBehaviourIssue(BehaviourIssue(1, "Pulling"))
            editor.toggleBehaviourIssue(BehaviourIssue(2, "Barking"))
            assertEquals(setOf(1L, 2L), editor.state.value.selectedBehaviourIssueIds)
            editor.toggleBehaviourIssue(BehaviourIssue(1, "Pulling"))
            assertEquals(setOf(2L), editor.state.value.selectedBehaviourIssueIds)
            editor.clearBehaviourIssues()
            assertTrue(editor.state.value.selectedBehaviourIssueIds.isEmpty())
            editor.addBehaviourIssue("  Digging ")
            assertEquals(BehaviourIssue(3, "Digging"), editor.state.value.behaviourIssues.last())
            assertEquals(setOf(3L), editor.state.value.selectedBehaviourIssueIds)
        }
    }

    @Test
    fun `concerns toggle on and off, clear together, and an added one is stored and selected`() {
        medicalConcernRepository.addConcerns("Limping", "Vomit")
        runEditorTest { editor ->
            editor.toggleMedicalConcern(MedicalConcern(1, "Limping"))
            editor.toggleMedicalConcern(MedicalConcern(2, "Vomit"))
            assertEquals(setOf(1L, 2L), editor.state.value.selectedMedicalConcernIds)
            editor.toggleMedicalConcern(MedicalConcern(1, "Limping"))
            assertEquals(setOf(2L), editor.state.value.selectedMedicalConcernIds)
            editor.clearMedicalConcerns()
            assertTrue(editor.state.value.selectedMedicalConcernIds.isEmpty())
            editor.addMedicalConcern("  Coughing ")
            assertEquals(MedicalConcern(3, "Coughing"), editor.state.value.medicalConcerns.last())
            assertEquals(setOf(3L), editor.state.value.selectedMedicalConcernIds)
        }
    }

    @Test
    fun `notes are cut at the maximum length`() = runEditorTest { editor ->
        editor.setNotes("x".repeat(MAX_NOTES_LENGTH + 1))
        assertEquals(MAX_NOTES_LENGTH, editor.state.value.notes.length)
    }

    @Test
    fun `prefill copies the walk and opens the sections that have content, with the stool folded`() = runEditorTest { editor ->
        val stool = Stool(passed = true, quantity = StoolQuantity.NORMAL, colour = StoolColour.DARK_BROWN)
        editor.prefillFrom(
            walk.copy(
                notes = "Muddy",
                equipment = Equipment.HARNESS,
                rating = 3,
                snow = Snow.LIGHT,
                issues = listOf(BehaviourIssue(1, "Pulling")),
                concerns = listOf(MedicalConcern(4, "Limping")),
                optionalRatings = mapOf(OptionalRating.RESPONSIVENESS to 4),
                stool = stool,
            ),
        )
        with(editor.state.value) {
            assertEquals(3, rating)
            assertEquals(Equipment.HARNESS, equipment)
            assertEquals(Snow.LIGHT, snow)
            assertTrue(isWeatherExpanded)
            assertEquals(mapOf(OptionalRating.RESPONSIVENESS to 4), optionalRatings)
            assertTrue(isOptionalRatingsExpanded)
            assertEquals(stool, this.stool)
            assertFalse(isStoolExpanded)
            assertEquals(setOf(1L), selectedBehaviourIssueIds)
            assertTrue(isBehaviourIssuesExpanded)
            assertEquals(setOf(4L), selectedMedicalConcernIds)
            assertTrue(isMedicalConcernsExpanded)
            assertEquals("Muddy", notes)
        }
    }

    @Test
    fun `prefill leaves empty sections closed and keeps the equipment when the walk has none`() = runEditorTest { editor ->
        editor.selectEquipment(Equipment.HARNESS)
        editor.prefillFrom(walk)
        with(editor.state.value) {
            assertEquals(Equipment.HARNESS, equipment)
            assertFalse(isOptionalRatingsExpanded)
            assertFalse(isWeatherExpanded)
            assertFalse(isBehaviourIssuesExpanded)
            assertFalse(isMedicalConcernsExpanded)
        }
    }

    @Test
    fun `apply writes the form into the walk with trimmed notes and the selected issues and concerns`() {
        behaviourIssueRepository.addIssues("Pulling", "Barking")
        medicalConcernRepository.addConcerns("Limping", "Vomit")
        runEditorTest { editor ->
            editor.setRating(6)
            editor.setOptionalRating(OptionalRating.ENERGY_LEVEL, 2)
            editor.selectStoolPassed(true)
            editor.selectStoolTexture(StoolTexture.FIRM)
            editor.selectEquipment(Equipment.HARNESS)
            editor.selectRain(Rain.NONE)
            editor.selectTemperature(Temperature.WARM)
            editor.selectWind(Wind.STILL)
            editor.selectSnow(Snow.NONE)
            editor.toggleBehaviourIssue(BehaviourIssue(2, "Barking"))
            editor.toggleMedicalConcern(MedicalConcern(1, "Limping"))
            editor.setNotes("  Off-lead in the field  ")

            assertEquals(
                walk.copy(
                    notes = "Off-lead in the field",
                    equipment = Equipment.HARNESS,
                    rating = 6,
                    optionalRatings = mapOf(OptionalRating.ENERGY_LEVEL to 2),
                    stool = Stool(passed = true, texture = StoolTexture.FIRM),
                    rain = Rain.NONE,
                    temperature = Temperature.WARM,
                    wind = Wind.STILL,
                    snow = Snow.NONE,
                    issues = listOf(BehaviourIssue(2, "Barking")),
                    concerns = listOf(MedicalConcern(1, "Limping")),
                ),
                editor.applyTo(walk),
            )
        }
    }
}
