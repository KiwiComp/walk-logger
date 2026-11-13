package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition

/** The weather section of the walk form, from fetching to the fallback place. */
data class WeatherFormState(
    /** The fetched or stored weather; precipitation and wind come only from here. */
    val fetchedWeather: WalkWeather? = null,
    /** The temperature as typed, in °C; prefilled from [fetchedWeather]. */
    val temperatureInput: String = "",
    val condition: WeatherCondition? = null,
    /** Set once the handler changes or clears the weather, so new walk times no longer refetch it. */
    val isEditedManually: Boolean = false,
    val status: WeatherStatus = WeatherStatus.IDLE,
    /** True once the walk has both times, so there is something to fetch the weather for. */
    val hasTimes: Boolean = false,
    /** True while [fetchedWeather] was fetched for the walk's current times. */
    val isCurrent: Boolean = false,
    /** False shows the place row, the fallback when the device location is unavailable. */
    val hasLocationPermission: Boolean = false,
    /** Set when the Route should show the system permission dialog. */
    val shouldRequestLocationPermission: Boolean = false,
    val place: Place? = null,
    /** Null while no place search is open. */
    val placeSearch: PlaceSearch? = null,
    /** True while the temperature field and condition picker are open over fetched weather. */
    val isEditing: Boolean = false,
    /** True while the weather is shown; opens by itself when there is weather or something to say about it. */
    val isExpanded: Boolean = false,
) {
    /** Null when the field is empty or not a number; accepts a decimal comma. */
    val temperatureCelsius: Double? get() = temperatureInput.replace(',', '.').toDoubleOrNull()

    /**
     * The fetch button only shows when fetching can bring something new. Too-old walks would fail again,
     * and without a location the place row is the way forward.
     */
    val showsFetch: Boolean
        get() = hasTimes && when (status) {
            WeatherStatus.LOADING, WeatherStatus.FAILED -> true
            WeatherStatus.TOO_OLD, WeatherStatus.NO_LOCATION -> false
            WeatherStatus.IDLE -> !isCurrent
        }

    /** Without fetched weather, entering it by hand is the only way, so the fields need no Edit tap. */
    val showsManualEntry: Boolean
        get() = (isEditing || fetchedWeather == null) && status != WeatherStatus.LOADING

    /** What will be saved: the entered temperature and condition over the fetched precipitation and wind. */
    val walkWeather: WalkWeather?
        get() = WalkWeather(
            condition = condition,
            isDay = fetchedWeather?.isDay ?: true,
            temperatureCelsius = temperatureCelsius,
            rainMm = fetchedWeather?.rainMm,
            snowfallCm = fetchedWeather?.snowfallCm,
            windSpeedMs = fetchedWeather?.windSpeedMs,
            windGustsMs = fetchedWeather?.windGustsMs,
        ).takeUnless { it.isEmpty }
}

enum class WeatherStatus { IDLE, LOADING, TOO_OLD, NO_LOCATION, FAILED }

/** [results] is null until the first search has answered. */
data class PlaceSearch(
    val results: List<Place>? = null,
    val isSearching: Boolean = false,
    val isFailed: Boolean = false,
)
