package com.example.walklogger.feature.logwalk

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherFormatTest {

    @Test
    fun `values that round to zero show without a minus sign`() {
        assertEquals("0", formatWeatherValue(-0.0))
        assertEquals("0", formatWeatherValue(-0.04))
    }

    @Test
    fun `a formatted input parses back in locales with their own minus signs and digits`() {
        val defaultLocale = Locale.getDefault()
        try {
            listOf("sv-SE", "he-IL", "ar-EG", "fa-IR").forEach { tag ->
                Locale.setDefault(Locale.forLanguageTag(tag))
                val state = WeatherFormState(temperatureInput = formatWeatherInput(-2.5))
                assertEquals(tag, -2.5, state.temperatureCelsius!!, 0.0)
            }
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }
}
