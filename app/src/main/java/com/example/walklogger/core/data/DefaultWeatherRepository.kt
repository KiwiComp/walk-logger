package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.network.OpenMeteoForecastApi
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DefaultWeatherRepository @Inject constructor(
    private val api: OpenMeteoForecastApi,
    private val clock: Clock,
    @Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : WeatherRepository {

    override fun isAvailable(time: Instant): Boolean = time >= clock.instant()
        .truncatedTo(ChronoUnit.DAYS)
        .minus(Duration.ofDays(OpenMeteoForecastApi.MAX_PAST_DAYS))

    override suspend fun weatherAt(coordinates: Coordinates, time: Instant): WalkWeather =
        withContext(ioDispatcher) {
            val hour = HOUR_FORMAT.format(time.truncatedTo(ChronoUnit.HOURS))
            val forecast = api.hourlyForecast(
                latitude = coordinates.latitude,
                longitude = coordinates.longitude,
                startHour = hour,
                endHour = hour,
            )
            checkNotNull(forecast.hourly.toWalkWeather(index = 0)) { "no weather for $hour" }
        }

    private companion object {
        val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd'T'HH:mm",
        ).withZone(ZoneOffset.UTC)
    }
}
