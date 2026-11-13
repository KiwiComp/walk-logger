package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WeatherCondition

/** The events of the walk form's weather section; all the UI can reach of [WeatherFormEditor]. */
interface WeatherFormActions {
    fun setTemperature(text: String)

    /** Picking the selected condition again removes it. */
    fun selectCondition(value: WeatherCondition)
    fun fetchWeather()
    fun onLocationPermissionResult(granted: Boolean)
    fun openPlaceSearch()
    fun searchPlaces(query: String)

    /** Saves [place] as the fallback location and fetches the weather for it. */
    fun selectPlace(place: Place)
    fun closePlaceSearch()
    fun toggleWeather()
    fun toggleWeatherEditing()
    fun clearWeather()
}
