package com.example.walklogger.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkWeatherTest {

    @Test
    fun `weather entered by hand is not fetched`() {
        assertFalse(
            WalkWeather(condition = WeatherCondition.FOG, temperatureCelsius = 3.0).isFetched,
        )
    }

    @Test
    fun `precipitation or wind means the weather was fetched`() {
        assertTrue(WalkWeather(rainMm = 0.0).isFetched)
        assertTrue(WalkWeather(snowfallCm = 0.3).isFetched)
        assertTrue(WalkWeather(windSpeedMs = 4.0).isFetched)
    }
}
