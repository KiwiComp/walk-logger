package com.example.walklogger.feature.logwalk

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/** One decimal at most, in the device locale ("7,5" in Swedish). */
internal fun formatWeatherValue(value: Double): String =
    DecimalFormat("0.#", DecimalFormatSymbols.getInstance()).format(value)
