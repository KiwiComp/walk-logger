package com.example.walklogger.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TemperatureBandTest {

    @Test
    fun `boundaries go to the colder band below 10 degrees and the warmer band from 10`() {
        val expected = listOf(
            -35.0 to TemperatureBand.EXTREMELY_COLD,
            -20.0 to TemperatureBand.EXTREMELY_COLD,
            -19.9 to TemperatureBand.VERY_COLD,
            -10.0 to TemperatureBand.VERY_COLD,
            -9.9 to TemperatureBand.COLD,
            0.0 to TemperatureBand.COLD,
            0.1 to TemperatureBand.COOL,
            9.9 to TemperatureBand.COOL,
            10.0 to TemperatureBand.MILD,
            19.9 to TemperatureBand.MILD,
            20.0 to TemperatureBand.WARM,
            29.9 to TemperatureBand.WARM,
            30.0 to TemperatureBand.VERY_WARM,
            38.0 to TemperatureBand.VERY_WARM,
        )
        expected.forEach { (celsius, band) -> assertEquals("$celsius °C", band, TemperatureBand.of(celsius)) }
    }

    @Test
    fun `the band follows the walk's temperature`() {
        assertEquals(TemperatureBand.COOL, WalkWeather(temperatureCelsius = 7.0).temperatureBand)
        assertEquals(null, WalkWeather(condition = WeatherCondition.FOG).temperatureBand)
    }
}
