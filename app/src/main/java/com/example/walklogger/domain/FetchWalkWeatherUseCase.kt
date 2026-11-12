package com.example.walklogger.domain

import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.PlaceRepository
import com.example.walklogger.core.data.WeatherRepository
import com.example.walklogger.core.location.LocationProvider
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.network.OpenMeteoForecastApi
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
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
    private val locationProvider: LocationProvider,
    private val placeRepository: PlaceRepository,
    private val weatherRepository: WeatherRepository,
    private val clock: Clock,
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
        val earliest = clock.instant()
            .truncatedTo(ChronoUnit.DAYS)
            .minus(Duration.ofDays(OpenMeteoForecastApi.MAX_PAST_DAYS))
        if (middle < earliest) return WeatherFetchResult.TooOld
        return runCatchingCancellable {
            val coordinates = walkId?.let { activeWalkCoordinates(it) }
                ?: deviceCoordinates()
                ?: placeRepository.savedPlace.first()?.coordinates
                ?: return@runCatchingCancellable WeatherFetchResult.NoLocation
            WeatherFetchResult.Fetched(weatherRepository.weatherAt(coordinates, middle))
        }.getOrDefault(WeatherFetchResult.Failed)
    }

    private suspend fun activeWalkCoordinates(walkId: Long) =
        placeRepository.activeWalkLocation.first()?.takeIf { it.walkId == walkId }?.coordinates

    private suspend fun deviceCoordinates() =
        if (locationProvider.hasPermission()) locationProvider.currentLocation() else null
}
