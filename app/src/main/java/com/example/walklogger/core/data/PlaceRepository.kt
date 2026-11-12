package com.example.walklogger.core.data

import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkLocation
import kotlinx.coroutines.flow.Flow

/** Where to fetch the weather when the device location is unavailable. */
interface PlaceRepository {
    val savedPlace: Flow<Place?>

    suspend fun savePlace(place: Place)

    /** Throws when the search cannot be made. */
    suspend fun searchPlaces(query: String): List<Place>

    /** True once the app has asked for location, wherever it asked; it never asks again by itself. */
    val locationPermissionRequested: Flow<Boolean>

    suspend fun markLocationPermissionRequested()

    /** Read when the walk ends so the weather fetch doesn't wait for a new position. */
    val activeWalkLocation: Flow<WalkLocation?>

    suspend fun saveActiveWalkLocation(location: WalkLocation)

    suspend fun clearActiveWalkLocation()
}
