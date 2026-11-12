package com.example.walklogger.core.data

import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.network.model.NetworkHourly
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherMappersTest {

    @Test
    fun `wmo codes map to conditions`() {
        val expected = mapOf(
            WeatherCondition.CLEAR to listOf(0),
            WeatherCondition.PARTLY_CLOUDY to listOf(1, 2),
            WeatherCondition.CLOUDY to listOf(3),
            WeatherCondition.FOG to listOf(45, 48),
            WeatherCondition.LIGHT_RAIN to listOf(51, 53, 55, 56, 57, 61, 80),
            WeatherCondition.RAIN to listOf(63, 81),
            WeatherCondition.HEAVY_RAIN to listOf(65, 82),
            WeatherCondition.FREEZING_RAIN to listOf(66, 67),
            WeatherCondition.LIGHT_SNOW to listOf(71, 77, 85),
            WeatherCondition.SNOW to listOf(73),
            WeatherCondition.HEAVY_SNOW to listOf(75, 86),
            WeatherCondition.THUNDERSTORM to listOf(95, 96, 99),
        )
        expected.forEach { (condition, codes) -> codes.forEach { assertEquals("code $it", condition, weatherConditionOf(it)) } }
        assertNull(weatherConditionOf(4))
    }

    @Test
    fun `an hour maps to the walk weather with showers counted as rain and night kept`() {
        val hourly = NetworkHourly(
            time = listOf(1_794_132_000),
            temperature = listOf(-1.5),
            weatherCode = listOf(80),
            isDay = listOf(0),
            rain = listOf(0.2),
            showers = listOf(0.3),
            snowfall = listOf(0.0),
            windSpeed = listOf(3.1),
            windGusts = listOf(6.4),
        )
        assertEquals(
            WalkWeather(
                condition = WeatherCondition.LIGHT_RAIN,
                isDay = false,
                temperatureCelsius = -1.5,
                rainMm = 0.5,
                snowfallCm = 0.0,
                windSpeedMs = 3.1,
                windGustsMs = 6.4,
            ),
            hourly.toWalkWeather(index = 0),
        )
    }

    @Test
    fun `a missing hour or an hour without values gives no weather`() {
        assertNull(NetworkHourly(time = emptyList()).toWalkWeather(index = 0))
        assertNull(NetworkHourly(time = listOf(1), temperature = listOf(null)).toWalkWeather(index = 0))
    }
}
