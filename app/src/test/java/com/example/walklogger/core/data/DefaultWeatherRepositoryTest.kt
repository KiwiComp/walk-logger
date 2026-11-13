package com.example.walklogger.core.data

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.network.OpenMeteoForecastApi
import com.example.walklogger.core.network.OpenMeteoJson
import com.example.walklogger.core.network.model.NetworkForecast
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultWeatherRepositoryTest {

    /** A real response, trimmed to one hour. */
    private val response = """
        {"latitude":59.3289,"longitude":18.072357,"generationtime_ms":0.63,"utc_offset_seconds":0,"timezone":"GMT",
        "hourly_units":{"time":"unixtime","temperature_2m":"°C"},
        "hourly":{"time":[1794132000],"temperature_2m":[2.5],"weather_code":[0],"is_day":[1],"rain":[0.00],
        "showers":[0.00],"snowfall":[0.00],"wind_speed_10m":[2.50],"wind_gusts_10m":[5.30]}}
    """.trimIndent()

    private val api = FakeForecastApi(OpenMeteoJson.decodeFromString(response))
    private val clock = Clock.fixed(Instant.parse("2026-11-09T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `asks for the hour containing the time and maps the response`() = runTest {
        val repository = DefaultWeatherRepository(api, clock, StandardTestDispatcher(testScheduler))
        val weather = repository.weatherAt(
            Coordinates(59.33, 18.07),
            Instant.parse("2026-11-08T10:42:10Z"),
        )
        assertEquals(
            WalkWeather(
                condition = WeatherCondition.CLEAR,
                isDay = true,
                temperatureCelsius = 2.5,
                rainMm = 0.0,
                snowfallCm = 0.0,
                windSpeedMs = 2.5,
                windGustsMs = 5.3,
            ),
            weather,
        )
        assertEquals(listOf("2026-11-08T10:00" to "2026-11-08T10:00"), api.requestedHours)
    }

    @Test
    fun `reaches back 92 days from the start of today`() = runTest {
        val repository = DefaultWeatherRepository(api, clock, StandardTestDispatcher(testScheduler))
        assertTrue(repository.isAvailable(Instant.parse("2026-08-09T00:00:00Z")))
        assertFalse(repository.isAvailable(Instant.parse("2026-08-08T23:59:59Z")))
    }

    @Test
    fun `an empty response throws`() = runTest {
        api.forecast = OpenMeteoJson.decodeFromString("""{"hourly":{"time":[]}}""")
        val repository = DefaultWeatherRepository(api, clock, StandardTestDispatcher(testScheduler))
        val result = runCatching { repository.weatherAt(Coordinates(0.0, 0.0), Instant.EPOCH) }
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    private class FakeForecastApi(var forecast: NetworkForecast) : OpenMeteoForecastApi {
        val requestedHours = mutableListOf<Pair<String, String>>()

        override suspend fun hourlyForecast(
            latitude: Double,
            longitude: Double,
            startHour: String,
            endHour: String,
            hourly: String,
            windSpeedUnit: String,
            timeFormat: String,
            timezone: String,
        ): NetworkForecast {
            requestedHours += startHour to endHour
            return forecast
        }
    }
}
