package com.example.walklogger.feature.logwalk

import com.example.walklogger.R
import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.reportUnlessOffline
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.domain.WeatherFetchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the weather section of the walk form: fetching for the walk's times, the handler's corrections and the
 * fallback place. [walkId] is the active walk being finished, whose stored start position the fetch uses.
 */
class WeatherFormEditor(
    private val scope: CoroutineScope,
    private val fetchWalkWeather: FetchWalkWeatherUseCase,
    private val locationPermission: LocationPermissionUseCase,
    private val locationRepository: LocationRepository,
    private val showError: (Throwable, Int, String) -> Unit,
    private val errorReporter: ErrorReporter,
    private val walkId: Long? = null,
) : WeatherFormActions {

    private val input =
        MutableStateFlow(
            WeatherFormState(hasLocationPermission = locationPermission.hasPermission()),
        )

    val state: StateFlow<WeatherFormState> = input.asStateFlow()

    /** Start and end in epoch millis, the times [fetchWeather] fetches for. */
    private var times: Pair<Long, Long>? = null

    /** The times [WeatherFormState.fetchedWeather] was fetched for; null when it is missing or was entered by hand. */
    private var fetchedFor: Pair<Long, Long>? = null

    /** Set by [prefillFrom] when the stored weather was fetched, so it counts as fetched for the stored times. */
    private var isPrefilledWeatherFetched = false

    private var weatherJob: Job? = null
    private var placeSearchJob: Job? = null

    init {
        scope.launch {
            locationRepository.savedPlace.collect { place ->
                input.update { it.copy(place = place) }
            }
        }
    }

    /** Shows the stored [weather], opening the section when there is any. */
    fun prefillFrom(weather: WalkWeather?) {
        isPrefilledWeatherFetched = weather?.isFetched == true
        input.update {
            it.copy(
                fetchedWeather = weather,
                temperatureInput = weather?.temperatureCelsius?.let(::formatWeatherValue).orEmpty(),
                condition = weather?.condition,
                isExpanded = weather != null,
                isEditing = false,
            )
        }
    }

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
                hasTimes = true,
                isCurrent = fetchedFor == times,
                status = if (it.status == WeatherStatus.LOADING) it.status else WeatherStatus.IDLE,
            )
        }
        if (autoFetch && !input.value.isEditedManually) fetchWeather()
    }

    override fun setTemperature(text: String) = input.update {
        it.copy(
            temperatureInput = text.filter { c ->
                c.isDigit() || c in TEMPERATURE_SYMBOLS
            }.take(MAX_TEMPERATURE_LENGTH),
            isEditedManually = true,
        )
    }

    override fun selectCondition(value: WeatherCondition) = input.update {
        it.copy(
            condition = if (it.condition == value) null else value,
            isEditedManually = true,
        )
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
            input.update { it.copy(status = WeatherStatus.LOADING, isExpanded = true) }
            val result = fetchWalkWeather(start, end, walkId)
            if (result is WeatherFetchResult.Fetched) fetchedFor = start to end
            input.update {
                it.withResult(result).copy(
                    hasLocationPermission = locationPermission.hasPermission(),
                    isCurrent = fetchedFor == times,
                )
            }
        }
    }

    override fun onLocationPermissionResult(granted: Boolean) {
        input.update {
            it.copy(shouldRequestLocationPermission = false, hasLocationPermission = granted)
        }
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
            input.update {
                it.copy(placeSearch = it.placeSearch?.copy(isSearching = true, isFailed = false))
            }
            runCatchingCancellable { locationRepository.searchPlaces(query) }
                .onSuccess { places ->
                    input.update {
                        it.copy(
                            placeSearch = it.placeSearch?.copy(
                                results = places,
                                isSearching = false,
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    errorReporter.reportUnlessOffline(error, "Failed to search places")
                    input.update {
                        it.copy(
                            placeSearch = it.placeSearch?.copy(
                                isSearching = false,
                                isFailed = true,
                            ),
                        )
                    }
                }
        }
    }

    override fun selectPlace(place: Place) {
        placeSearchJob?.cancel()
        input.update { it.copy(placeSearch = null) }
        scope.launch {
            runCatchingCancellable { locationRepository.savePlace(place) }
                .onSuccess { fetchWeather() }
                .onFailure { showError(it, R.string.error_save_place, "Failed to save place") }
        }
    }

    override fun closePlaceSearch() {
        placeSearchJob?.cancel()
        input.update { it.copy(placeSearch = null) }
    }

    override fun toggleWeather() = input.update { it.copy(isExpanded = !it.isExpanded) }

    override fun toggleWeatherEditing() = input.update { it.copy(isEditing = !it.isEditing) }

    override fun clearWeather() {
        weatherJob?.cancel()
        fetchedFor = null
        input.update {
            it.copy(
                fetchedWeather = null,
                isCurrent = false,
                isEditing = false,
                temperatureInput = "",
                condition = null,
                isEditedManually = true,
                status = WeatherStatus.IDLE,
            )
        }
    }

    private fun WeatherFormState.withResult(result: WeatherFetchResult): WeatherFormState =
        when (result) {
            is WeatherFetchResult.Fetched -> copy(
                fetchedWeather = result.weather,
                temperatureInput = result.weather.temperatureCelsius?.let(::formatWeatherValue)
                    .orEmpty(),
                condition = result.weather.condition,
                isEditedManually = false,
                isEditing = false,
                status = WeatherStatus.IDLE,
            )

            WeatherFetchResult.TooOld -> copy(status = WeatherStatus.TOO_OLD)

            WeatherFetchResult.NoLocation -> copy(status = WeatherStatus.NO_LOCATION)

            WeatherFetchResult.Failed -> copy(status = WeatherStatus.FAILED)
        }

    private companion object {
        const val MAX_TEMPERATURE_LENGTH = 6
        const val TEMPERATURE_SYMBOLS = "-.,"
    }
}
