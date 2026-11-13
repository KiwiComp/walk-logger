package com.example.walklogger.testing

import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [LocationRepository] for unit tests; grant or deny with [isGranted], place the device with [location],
 * and [searchResults] is what every place search returns.
 */
class FakeLocationRepository(
    var isGranted: Boolean = true,
    var location: Coordinates? = Coordinates(59.33, 18.07),
) : LocationRepository {

    val failures = FailureSwitch()

    var searchResults: List<Place> = emptyList()

    /** While on, searches throw. */
    var failSearches: Boolean = false

    private val requested = MutableStateFlow(false)
    private val place = MutableStateFlow<Place?>(null)

    override fun hasPermission(): Boolean = isGranted

    override suspend fun currentLocation(): Coordinates? = if (isGranted) location else null

    override val locationPermissionRequested: Flow<Boolean> = requested

    override suspend fun markLocationPermissionRequested() {
        requested.value = true
    }

    override val savedPlace: Flow<Place?> = place

    override suspend fun savePlace(place: Place) {
        failures.check()
        this.place.value = place
    }

    override suspend fun searchPlaces(query: String): List<Place> {
        if (failSearches) throw IOException("simulated search failure")
        return searchResults
    }
}
