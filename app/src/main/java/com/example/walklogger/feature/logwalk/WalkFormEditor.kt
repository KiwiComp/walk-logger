package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.Wind
import com.example.walklogger.core.model.withColour
import com.example.walklogger.core.model.withPassed
import com.example.walklogger.core.model.withQuantity
import com.example.walklogger.core.model.withTexture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Holds the shared walk form for a ViewModel, which keeps it private and hands the UI only [WalkFormActions]. */
class WalkFormEditor(
    private val scope: CoroutineScope,
    private val behaviourIssueRepository: BehaviourIssueRepository,
    private val medicalConcernRepository: MedicalConcernRepository,
) : WalkFormActions {

    /** What the user has entered; the issue and concern lists are overlaid from their repositories. */
    private val input = MutableStateFlow(WalkFormState())

    /** Shared eagerly so [applyTo] reads current lists even while nothing is collecting. */
    val state: StateFlow<WalkFormState> =
        combine(
            input,
            behaviourIssueRepository.observeBehaviourIssues(),
            medicalConcernRepository.observeMedicalConcerns(),
        ) { input, issues, concerns ->
            input.copy(behaviourIssues = issues, medicalConcerns = concerns)
        }.stateIn(scope, SharingStarted.Eagerly, input.value)

    /** Opens the optional sections that have content; a stored stool answer starts folded. */
    fun prefillFrom(walk: Walk) = input.update {
        it.copy(
            rating = walk.rating,
            optionalRatings = walk.optionalRatings,
            isOptionalRatingsExpanded = walk.optionalRatings.isNotEmpty(),
            stool = walk.stool,
            isStoolExpanded = false,
            equipment = walk.equipment ?: it.equipment,
            rain = walk.rain,
            temperature = walk.temperature,
            wind = walk.wind,
            snow = walk.snow,
            isWeatherExpanded = listOf(walk.rain, walk.temperature, walk.wind, walk.snow).any { it != null },
            selectedBehaviourIssueIds = walk.behaviourIssues.map(BehaviourIssue::id).toSet(),
            isBehaviourIssuesExpanded = walk.behaviourIssues.isNotEmpty(),
            selectedMedicalConcernIds = walk.medicalConcerns.map(MedicalConcern::id).toSet(),
            isMedicalConcernsExpanded = walk.medicalConcerns.isNotEmpty(),
            notes = walk.notes,
        )
    }

    /** [walk] with the form's fields written into it. */
    fun applyTo(walk: Walk): Walk {
        val form = state.value
        return walk.copy(
            notes = form.notes.trim(),
            equipment = form.equipment,
            rating = form.rating,
            optionalRatings = form.optionalRatings,
            stool = form.stool,
            rain = form.rain,
            temperature = form.temperature,
            wind = form.wind,
            snow = form.snow,
            behaviourIssues = form.behaviourIssues.filter { it.id in form.selectedBehaviourIssueIds },
            medicalConcerns = form.medicalConcerns.filter { it.id in form.selectedMedicalConcernIds },
        )
    }

    override fun setRating(rating: Int) = input.update { it.copy(rating = rating) }

    override fun setOptionalRating(aspect: OptionalRating, value: Int?) = input.update {
        it.copy(optionalRatings = if (value == null) it.optionalRatings - aspect else it.optionalRatings + (aspect to value))
    }

    override fun toggleOptionalRatings() = input.update { it.copy(isOptionalRatingsExpanded = !it.isOptionalRatingsExpanded) }

    override fun clearOptionalRatings() = input.update { it.copy(optionalRatings = emptyMap()) }

    override fun selectStoolPassed(passed: Boolean) = input.update {
        val wasPassed = it.stool?.passed == true
        it.copy(
            stool = it.stool.withPassed(passed),
            isStoolExpanded = if (passed && !wasPassed) true else it.isStoolExpanded,
        )
    }

    override fun foldStool() = input.update { it.copy(isStoolExpanded = false) }

    override fun expandStool() = input.update { it.copy(isStoolExpanded = true) }

    override fun selectStoolQuantity(value: StoolQuantity) = input.update { it.copy(stool = it.stool.withQuantity(value)) }

    override fun selectStoolTexture(value: StoolTexture) = input.update { it.copy(stool = it.stool.withTexture(value)) }

    override fun selectStoolColour(value: StoolColour?) = input.update { it.copy(stool = it.stool.withColour(value)) }

    override fun clearStool() = input.update { it.copy(stool = null) }

    override fun selectEquipment(value: Equipment) = input.update { it.copy(equipment = value) }

    override fun selectRain(value: Rain) = input.update { it.copy(rain = value) }

    override fun selectTemperature(value: Temperature) = input.update { it.copy(temperature = value) }

    override fun selectWind(value: Wind) = input.update { it.copy(wind = value) }

    override fun selectSnow(value: Snow) = input.update { it.copy(snow = value) }

    override fun toggleWeather() = input.update { it.copy(isWeatherExpanded = !it.isWeatherExpanded) }

    override fun clearWeather() = input.update { it.copy(rain = null, temperature = null, wind = null, snow = null) }

    override fun toggleBehaviourIssue(issue: BehaviourIssue) = input.update { state ->
        val selected = state.selectedBehaviourIssueIds
        state.copy(selectedBehaviourIssueIds = if (issue.id in selected) selected - issue.id else selected + issue.id)
    }

    override fun addBehaviourIssue(name: String) {
        scope.launch {
            val stored = behaviourIssueRepository.addBehaviourIssue(name)
            input.update { it.copy(selectedBehaviourIssueIds = it.selectedBehaviourIssueIds + stored.id) }
        }
    }

    override fun toggleBehaviourIssuesExpanded() = input.update { it.copy(isBehaviourIssuesExpanded = !it.isBehaviourIssuesExpanded) }

    override fun clearBehaviourIssues() = input.update { it.copy(selectedBehaviourIssueIds = emptySet()) }

    override fun toggleMedicalConcern(concern: MedicalConcern) = input.update { state ->
        val selected = state.selectedMedicalConcernIds
        state.copy(selectedMedicalConcernIds = if (concern.id in selected) selected - concern.id else selected + concern.id)
    }

    override fun addMedicalConcern(name: String) {
        scope.launch {
            val stored = medicalConcernRepository.addMedicalConcern(name)
            input.update { it.copy(selectedMedicalConcernIds = it.selectedMedicalConcernIds + stored.id) }
        }
    }

    override fun toggleMedicalConcernsExpanded() = input.update { it.copy(isMedicalConcernsExpanded = !it.isMedicalConcernsExpanded) }

    override fun clearMedicalConcerns() = input.update { it.copy(selectedMedicalConcernIds = emptySet()) }

    override fun setNotes(notes: String) = input.update { it.copy(notes = notes.take(MAX_NOTES_LENGTH)) }
}
