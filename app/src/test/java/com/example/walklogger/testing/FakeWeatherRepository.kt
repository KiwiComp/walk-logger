package com.example.walklogger.testing

import com.example.walklogger.core.data.WeatherRepository
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import java.io.IOException
import java.time.Instant

/**
 * [WeatherRepository] for unit tests: answers with [weather] and records each request in [requests];
 * times before [oldestAvailable] are out of reach.
 */
class FakeWeatherRepository : WeatherRepository {

    var weather = WalkWeather(
        condition = WeatherCondition.LIGHT_RAIN,
        temperatureCelsius = 7.5,
        rainMm = 0.4,
        snowfallCm = 0.0,
        windSpeedMs = 4.0,
        windGustsMs = 9.0,
    )

    /** While on, every request throws. */
    var fail: Boolean = false

    var oldestAvailable: Instant = Instant.MIN

    val requests = mutableListOf<Pair<Coordinates, Instant>>()

    override fun isAvailable(time: Instant): Boolean = time >= oldestAvailable

    override suspend fun weatherAt(coordinates: Coordinates, time: Instant): WalkWeather {
        requests += coordinates to time
        if (fail) throw IOException("simulated network failure")
        return weather
    }
}
