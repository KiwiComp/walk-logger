package com.example.walklogger.feature.logwalk

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** One decimal at most, in the device locale ("7,5" and "−2,5" in Swedish). */
internal fun formatWeatherValue(value: Double): String =
    format(value, DecimalFormatSymbols.getInstance())

/**
 * Like [formatWeatherValue], but with ASCII digits, an ASCII minus and a `,` or `.` decimal separator in
 * every locale, so [WeatherFormState.temperatureCelsius] can parse it back. Some locales' minus signs
 * (U+2212, or one with a direction mark in Hebrew and Arabic) and digits would not parse.
 */
internal fun formatWeatherInput(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.ROOT).apply {
        decimalSeparator =
            if (DecimalFormatSymbols.getInstance().decimalSeparator == ',') ',' else '.'
    }
    return format(value, symbols)
}

/** Rounded first and `+ 0.0` turns -0.0 into 0.0, so values such as -0.04 show as "0", not "-0". */
private fun format(value: Double, symbols: DecimalFormatSymbols): String =
    DecimalFormat("0.#", symbols).format(Math.rint(value * 10) / 10 + 0.0)
