package com.example.walklogger.core.data

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import kotlinx.coroutines.flow.Flow

/** Where the device is, whether the app has asked for location, and the saved place to fall back on without it. */
interface LocationRepository {
    /** True with either coarse or fine location, so a later precise-location grant still counts. */
    fun hasPermission(): Boolean

    /** Null without permission or when the device cannot tell where it is. */
    suspend fun currentLocation(): Coordinates?

    /** True once the app has asked for location, wherever it asked; it never asks again by itself. */
    val locationPermissionRequested: Flow<Boolean>

    suspend fun markLocationPermissionRequested()

    /** Where to fetch the weather when the device location is unavailable. */
    val savedPlace: Flow<Place?>

    suspend fun savePlace(place: Place)

    /** Throws when the search cannot be made. */
    suspend fun searchPlaces(query: String): List<Place>
}
