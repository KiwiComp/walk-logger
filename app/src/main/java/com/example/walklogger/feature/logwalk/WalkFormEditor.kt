package com.example.walklogger.feature.logwalk

import com.example.walklogger.R
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.data.PlaceRepository
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.model.withColour
import com.example.walklogger.core.model.withPassed
import com.example.walklogger.core.model.withQuantity
import com.example.walklogger.core.model.withTexture
import com.example.walklogger.core.ui.message.UserMessages
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.feature.logwalk.component.formatWeatherValue
import com.example.walklogger.domain.WeatherFetchResult
import kotlinx.coroutines.Job
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
 * [walkId] is the active walk being finished, whose stored start position the weather fetch uses.
 */
class WalkFormEditor(
    private val scope: CoroutineScope,
    private val behaviourIssueRepository: BehaviourIssueRepository,
    private val medicalConcernRepository: MedicalConcernRepository,
    private val userMessages: UserMessages,
    private val fetchWalkWeather: FetchWalkWeatherUseCase,
    private val locationPermission: LocationPermissionUseCase,
    private val placeRepository: PlaceRepository,
    private val walkId: Long? = null,
) : WalkFormActions {

    /** What the user has entered; the issue and concern lists are overlaid from their repositories. */
    private val input = MutableStateFlow(WalkFormState(hasLocationPermission = locationPermission.hasPermission()))

    /** Start and end in epoch millis, the times [fetchWeather] fetches for. */
    private var times: Pair<Long, Long>? = null

    /** The times [WalkFormState.fetchedWeather] was fetched for; null when it is missing or was entered by hand. */
    private var fetchedFor: Pair<Long, Long>? = null

    /** Set by [prefillFrom] when the stored weather came from the API, so it counts as fetched for the stored times. */
    private var isPrefilledWeatherFetched = false

    private var weatherJob: Job? = null
    private var placeSearchJob: Job? = null

    init {
        scope.launch {
            placeRepository.savedPlace.collect { place -> input.update { it.copy(place = place) } }
        }
    }

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
    fun prefillFrom(walk: Walk) {
        isPrefilledWeatherFetched = walk.weather?.let { w -> listOf(w.rainMm, w.snowfallCm, w.windSpeedMs).any { it != null } } == true
        input.update {
            it.copy(
                rating = walk.rating,
                optionalRatings = walk.optionalRatings,
                isOptionalRatingsExpanded = walk.optionalRatings.isNotEmpty(),
                stool = walk.stool,
                isStoolExpanded = false,
                equipment = walk.equipment ?: it.equipment,
                fetchedWeather = walk.weather,
                temperatureInput = walk.weather?.temperatureCelsius?.let(::formatWeatherValue).orEmpty(),
                condition = walk.weather?.condition,
                isWeatherExpanded = walk.weather != null,
                isWeatherEditing = false,
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
            weather = form.weather,
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

    /**
     * Records the walk's times for [fetchWeather]. With [autoFetch] the weather is fetched for them,
     * unless the handler has already changed it by hand.
     */
    fun onTimesChanged(startMillis: Long, endMillis: Long, autoFetch: Boolean) {
        if (startMillis >= endMillis || times == startMillis to endMillis) return
        times = startMillis to endMillis
        if (isPrefilledWeatherFetched) {
            fetchedFor = times
            isPrefilledWeatherFetched = false
        }
        input.update {
            it.copy(
                hasWeatherTimes = true,
                isWeatherCurrent = fetchedFor == times,
                weatherStatus = if (it.weatherStatus == WeatherStatus.LOADING) it.weatherStatus else WeatherStatus.IDLE,
            )
        }
        if (autoFetch && !input.value.isWeatherEditedManually) fetchWeather()
    }

    override fun setTemperature(text: String) = input.update {
        it.copy(temperatureInput = text.filter { c -> c.isDigit() || c in TEMPERATURE_SYMBOLS }.take(MAX_TEMPERATURE_LENGTH), isWeatherEditedManually = true)
    }

    override fun selectCondition(value: WeatherCondition) = input.update {
        it.copy(condition = if (it.condition == value) null else value, isWeatherEditedManually = true)
    }

    /** Asks for location the first time; the answer arrives in [onLocationPermissionResult], which fetches again. */
    override fun fetchWeather() {
        val (start, end) = times ?: return
        weatherJob?.cancel()
        weatherJob = scope.launch {
            if (locationPermission.shouldRequest()) {
                input.update { it.copy(shouldRequestLocationPermission = true) }
                return@launch
            }
            input.update { it.copy(weatherStatus = WeatherStatus.LOADING, isWeatherExpanded = true) }
            val result = fetchWalkWeather(start, end, walkId)
            if (result is WeatherFetchResult.Fetched) fetchedFor = start to end
            input.update {
                it.withWeatherResult(result).copy(
                    hasLocationPermission = locationPermission.hasPermission(),
                    isWeatherCurrent = fetchedFor == times,
                )
            }
        }
    }

    override fun onLocationPermissionResult(granted: Boolean) {
        input.update { it.copy(shouldRequestLocationPermission = false, hasLocationPermission = granted) }
        scope.launch {
            locationPermission.markRequested()
            fetchWeather()
        }
    }

    override fun openPlaceSearch() = input.update { it.copy(placeSearch = PlaceSearch()) }

    override fun searchPlaces(query: String) {
        if (query.isBlank()) return
        placeSearchJob?.cancel()
        placeSearchJob = scope.launch {
            input.update { it.copy(placeSearch = it.placeSearch?.copy(isSearching = true, isFailed = false)) }
            runCatchingCancellable { placeRepository.searchPlaces(query) }
                .onSuccess { places -> input.update { it.copy(placeSearch = it.placeSearch?.copy(results = places, isSearching = false)) } }
                .onFailure { input.update { it.copy(placeSearch = it.placeSearch?.copy(isSearching = false, isFailed = true)) } }
        }
    }

    override fun selectPlace(place: Place) {
        placeSearchJob?.cancel()
        input.update { it.copy(placeSearch = null) }
        scope.launch {
            runCatchingCancellable { placeRepository.savePlace(place) }
                .onSuccess { fetchWeather() }
                .onFailure { userMessages.show(R.string.error_save_place) }
        }
    }

    override fun closePlaceSearch() {
        placeSearchJob?.cancel()
        input.update { it.copy(placeSearch = null) }
    }

    override fun toggleWeather() = input.update { it.copy(isWeatherExpanded = !it.isWeatherExpanded) }

    override fun toggleWeatherEditing() = input.update { it.copy(isWeatherEditing = !it.isWeatherEditing) }

    override fun clearWeather() {
        weatherJob?.cancel()
        fetchedFor = null
        input.update {
            it.copy(
                fetchedWeather = null,
                isWeatherCurrent = false,
                isWeatherEditing = false,
                temperatureInput = "",
                condition = null,
                isWeatherEditedManually = true,
                weatherStatus = WeatherStatus.IDLE,
            )
        }
    }

    private fun WalkFormState.withWeatherResult(result: WeatherFetchResult): WalkFormState = when (result) {
        is WeatherFetchResult.Fetched -> copy(
            fetchedWeather = result.weather,
            temperatureInput = result.weather.temperatureCelsius?.let(::formatWeatherValue).orEmpty(),
            condition = result.weather.condition,
            isWeatherEditedManually = false,
            isWeatherEditing = false,
            weatherStatus = WeatherStatus.IDLE,
        )
        WeatherFetchResult.TooOld -> copy(weatherStatus = WeatherStatus.TOO_OLD)
        WeatherFetchResult.NoLocation -> copy(weatherStatus = WeatherStatus.NO_LOCATION)
        WeatherFetchResult.Failed -> copy(weatherStatus = WeatherStatus.FAILED)
    }

    override fun toggleBehaviourIssue(issue: BehaviourIssue) = input.update { state ->
        val selected = state.selectedBehaviourIssueIds
        state.copy(selectedBehaviourIssueIds = if (issue.id in selected) selected - issue.id else selected + issue.id)
    }

    override fun addBehaviourIssue(name: String) {
        scope.launch {
            runCatchingCancellable { behaviourIssueRepository.addBehaviourIssue(name) }
                .onSuccess { stored ->
                    input.update { it.copy(selectedBehaviourIssueIds = it.selectedBehaviourIssueIds + stored.id) }
                }
                .onFailure { userMessages.show(R.string.error_add_name) }
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
            runCatchingCancellable { medicalConcernRepository.addMedicalConcern(name) }
                .onSuccess { stored ->
                    input.update { it.copy(selectedMedicalConcernIds = it.selectedMedicalConcernIds + stored.id) }
                }
                .onFailure { userMessages.show(R.string.error_add_name) }
        }
    }

    override fun toggleMedicalConcernsExpanded() = input.update { it.copy(isMedicalConcernsExpanded = !it.isMedicalConcernsExpanded) }

    override fun clearMedicalConcerns() = input.update { it.copy(selectedMedicalConcernIds = emptySet()) }

    override fun setNotes(notes: String) = input.update { it.copy(notes = notes.take(MAX_NOTES_LENGTH)) }

    private companion object {
        const val MAX_TEMPERATURE_LENGTH = 6
        const val TEMPERATURE_SYMBOLS = "-.,"
    }
}
