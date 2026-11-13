package com.example.walklogger.core.model

/**
 * The weather during a walk. Every value is optional: fetched values may be missing and the
 * handler can only enter [temperatureCelsius] and [condition] by hand.
 */
data class WalkWeather(
    val condition: WeatherCondition? = null,
    /** Picks the day or night icon for conditions that have both. */
    val isDay: Boolean = true,
    val temperatureCelsius: Double? = null,
    val rainMm: Double? = null,
    val snowfallCm: Double? = null,
    val windSpeedMs: Double? = null,
    val windGustsMs: Double? = null,
) {
    val temperatureBand: TemperatureBand? get() = temperatureCelsius?.let(TemperatureBand::of)

    val isEmpty: Boolean
        get() = listOf(
            condition,
            temperatureCelsius,
            rainMm,
            snowfallCm,
            windSpeedMs,
            windGustsMs,
        ).all {
            it ==
                null
        }

    /** Precipitation and wind are only ever fetched, never entered by hand, so either means the weather was fetched. */
    val isFetched: Boolean get() = listOf(rainMm, snowfallCm, windSpeedMs).any { it != null }
}

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    LIGHT_RAIN,
    RAIN,
    HEAVY_RAIN,
    FREEZING_RAIN,
    LIGHT_SNOW,
    SNOW,
    HEAVY_SNOW,
    THUNDERSTORM,
}

/** A boundary value belongs to the colder band below 10 °C and to the warmer band from 10 °C. */
enum class TemperatureBand {
    EXTREMELY_COLD,
    VERY_COLD,
    COLD,
    COOL,
    MILD,
    WARM,
    VERY_WARM,
    ;

    companion object {
        fun of(celsius: Double): TemperatureBand = when {
            celsius <= -20 -> EXTREMELY_COLD
            celsius <= -10 -> VERY_COLD
            celsius <= 0 -> COLD
            celsius < 10 -> COOL
            celsius < 20 -> MILD
            celsius < 30 -> WARM
            else -> VERY_WARM
        }
    }
}

data class Coordinates(val latitude: Double, val longitude: Double)

/** A town the weather can be fetched for when the device location is unavailable. */
data class Place(val name: String, val region: String?, val coordinates: Coordinates)

/** Where an active walk started, kept only until the walk is finished or discarded. */
data class WalkLocation(val walkId: Long, val coordinates: Coordinates)
