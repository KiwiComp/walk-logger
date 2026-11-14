package com.example.walklogger.feature.logwalk

import com.example.walklogger.R
import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.withColour
import com.example.walklogger.core.model.withPassed
import com.example.walklogger.core.model.withQuantity
import com.example.walklogger.core.model.withTexture
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the shared walk form for a ViewModel, which keeps it private and hands the UI only [WalkFormActions].
 * The weather section is its own [WeatherFormEditor], whose actions this editor passes on.
 */
class WalkFormEditor private constructor(
    private val scope: CoroutineScope,
    private val behaviourIssueRepository: BehaviourIssueRepository,
    private val medicalConcernRepository: MedicalConcernRepository,
    private val showError: (Throwable, Int, String) -> Unit,
    private val weather: WeatherFormEditor,
) : WalkFormActions,
    WeatherFormActions by weather {

    /** [walkId] is the active walk being finished, whose stored start position the weather fetch uses. */
    constructor(
        scope: CoroutineScope,
        behaviourIssueRepository: BehaviourIssueRepository,
        medicalConcernRepository: MedicalConcernRepository,
        showError: (Throwable, Int, String) -> Unit,
        errorReporter: ErrorReporter,
        fetchWalkWeather: FetchWalkWeatherUseCase,
        locationPermission: LocationPermissionUseCase,
        locationRepository: LocationRepository,
        walkId: Long? = null,
    ) : this(
        scope,
        behaviourIssueRepository,
        medicalConcernRepository,
        showError,
        WeatherFormEditor(
            scope,
            fetchWalkWeather,
            locationPermission,
            locationRepository,
            showError,
            errorReporter,
            walkId,
        ),
    )

    /** What the user has entered; the weather and the issue and concern lists are overlaid. */
    private val input = MutableStateFlow(WalkFormState())

    /** Shared eagerly so [applyTo] reads current lists even while nothing is collecting. */
    val state: StateFlow<WalkFormState> =
        combine(
            input,
            weather.state,
            behaviourIssueRepository.observeBehaviourIssues(),
            medicalConcernRepository.observeMedicalConcerns(),
        ) { input, weather, issues, concerns ->
            input.copy(weather = weather, behaviourIssues = issues, medicalConcerns = concerns)
        }.stateIn(scope, SharingStarted.Eagerly, input.value.copy(weather = weather.state.value))

    /** Opens the optional sections that have content; a stored stool answer starts folded. */
    fun prefillFrom(walk: Walk) {
        weather.prefillFrom(walk.weather)
        input.update {
            it.copy(
                rating = walk.rating,
                optionalRatings = walk.optionalRatings,
                isOptionalRatingsExpanded = walk.optionalRatings.isNotEmpty(),
                stool = walk.stool,
                isStoolExpanded = false,
                equipment = walk.equipment ?: it.equipment,
                selectedBehaviourIssueIds = walk.behaviourIssues.map(BehaviourIssue::id).toSet(),
                isBehaviourIssuesExpanded = walk.behaviourIssues.isNotEmpty(),
                selectedMedicalConcernIds = walk.medicalConcerns.map(MedicalConcern::id).toSet(),
                isMedicalConcernsExpanded = walk.medicalConcerns.isNotEmpty(),
                notes = walk.notes,
            )
        }
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
            weather = form.weather.walkWeather,
            behaviourIssues = form.behaviourIssues.filter {
                it.id in form.selectedBehaviourIssueIds
            },
            medicalConcerns = form.medicalConcerns.filter {
                it.id in form.selectedMedicalConcernIds
            },
        )
    }

    /** See [WeatherFormEditor.onTimesChanged]. */
    fun onTimesChanged(startMillis: Long, endMillis: Long, autoFetch: Boolean) =
        weather.onTimesChanged(startMillis, endMillis, autoFetch)

    override fun setRating(rating: Int) = input.update { it.copy(rating = rating) }

    override fun setOptionalRating(aspect: OptionalRating, value: Int?) = input.update {
        it.copy(
            optionalRatings = if (value == null) {
                it.optionalRatings - aspect
            } else {
                it.optionalRatings + (aspect to value)
            },
        )
    }

    override fun toggleOptionalRatings() = input.update {
        it.copy(isOptionalRatingsExpanded = !it.isOptionalRatingsExpanded)
    }

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

    override fun selectStoolQuantity(value: StoolQuantity) = input.update {
        it.copy(stool = it.stool.withQuantity(value))
    }

    override fun selectStoolTexture(value: StoolTexture) = input.update {
        it.copy(stool = it.stool.withTexture(value))
    }

    override fun selectStoolColour(value: StoolColour?) = input.update {
        it.copy(stool = it.stool.withColour(value))
    }

    override fun clearStool() = input.update { it.copy(stool = null) }

    override fun selectEquipment(value: Equipment) = input.update { it.copy(equipment = value) }

    override fun toggleBehaviourIssue(issue: BehaviourIssue) = input.update {
        it.copy(selectedBehaviourIssueIds = it.selectedBehaviourIssueIds.toggled(issue.id))
    }

    override fun addBehaviourIssue(name: String) {
        scope.launch {
            runCatchingCancellable { behaviourIssueRepository.addBehaviourIssue(name) }
                .onSuccess { stored ->
                    input.update {
                        it.copy(
                            selectedBehaviourIssueIds = it.selectedBehaviourIssueIds + stored.id,
                        )
                    }
                }
                .onFailure {
                    showError(it, R.string.error_add_name, "Failed to add behaviour issue")
                }
        }
    }

    override fun toggleBehaviourIssuesExpanded() = input.update {
        it.copy(isBehaviourIssuesExpanded = !it.isBehaviourIssuesExpanded)
    }

    override fun clearBehaviourIssues() = input.update {
        it.copy(selectedBehaviourIssueIds = emptySet())
    }

    override fun toggleMedicalConcern(concern: MedicalConcern) = input.update {
        it.copy(selectedMedicalConcernIds = it.selectedMedicalConcernIds.toggled(concern.id))
    }

    override fun addMedicalConcern(name: String) {
        scope.launch {
            runCatchingCancellable { medicalConcernRepository.addMedicalConcern(name) }
                .onSuccess { stored ->
                    input.update {
                        it.copy(
                            selectedMedicalConcernIds = it.selectedMedicalConcernIds + stored.id,
                        )
                    }
                }
                .onFailure {
                    showError(it, R.string.error_add_name, "Failed to add medical concern")
                }
        }
    }

    override fun toggleMedicalConcernsExpanded() = input.update {
        it.copy(isMedicalConcernsExpanded = !it.isMedicalConcernsExpanded)
    }

    override fun clearMedicalConcerns() = input.update {
        it.copy(selectedMedicalConcernIds = emptySet())
    }

    override fun setNotes(notes: String) = input.update {
        it.copy(notes = notes.take(MAX_NOTES_LENGTH))
    }

    private fun Set<Long>.toggled(id: Long): Set<Long> = if (id in this) this - id else this + id
}
