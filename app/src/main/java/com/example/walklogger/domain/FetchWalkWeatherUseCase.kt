package com.example.walklogger.domain

import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.ActiveWalkLocationRepository
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.data.WeatherRepository
import com.example.walklogger.core.model.WalkWeather
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

sealed interface WeatherFetchResult {
    data class Fetched(val weather: WalkWeather) : WeatherFetchResult

    /** Older than the free forecast API reaches. */
    data object TooOld : WeatherFetchResult

    /** Neither the device location nor a saved place is available. */
    data object NoLocation : WeatherFetchResult

    data object Failed : WeatherFetchResult
}

/** The weather for a walk, from the device location or, failing that, the saved place. */
class FetchWalkWeatherUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val activeWalkLocationRepository: ActiveWalkLocationRepository,
) {
    /**
     * Values are taken from the hour that contains the middle of the walk. A position stored for
     * [walkId] when the walk started is used before asking the device.
     */
    suspend operator fun invoke(
        startMillis: Long,
        endMillis: Long,
        walkId: Long? = null,
    ): WeatherFetchResult {
        val middle = Instant.ofEpochMilli(startMillis + (endMillis - startMillis) / 2)
        if (!weatherRepository.isAvailable(middle)) return WeatherFetchResult.TooOld
        return runCatchingCancellable {
            val coordinates = walkId?.let { activeWalkCoordinates(it) }
                ?: deviceCoordinates()
                ?: locationRepository.savedPlace.first()?.coordinates
                ?: return@runCatchingCancellable WeatherFetchResult.NoLocation
            WeatherFetchResult.Fetched(weatherRepository.weatherAt(coordinates, middle))
        }.getOrDefault(WeatherFetchResult.Failed)
    }

    private suspend fun activeWalkCoordinates(walkId: Long) =
        activeWalkLocationRepository.activeWalkLocation.first()?.takeIf {
            it.walkId == walkId
        }?.coordinates

    private suspend fun deviceCoordinates() =
        if (locationRepository.hasPermission()) locationRepository.currentLocation() else null
}
