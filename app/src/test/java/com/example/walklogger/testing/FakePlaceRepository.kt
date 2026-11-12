package com.example.walklogger.testing

import com.example.walklogger.core.data.PlaceRepository
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkLocation
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [PlaceRepository] for unit tests; [searchResults] is what every search returns. */
class FakePlaceRepository : PlaceRepository {

    val failures = FailureSwitch()

    var searchResults: List<Place> = emptyList()

    /** While on, searches throw. */
    var failSearches: Boolean = false

    private val place = MutableStateFlow<Place?>(null)
    private val requested = MutableStateFlow(false)

    override val savedPlace: Flow<Place?> = place

    override suspend fun savePlace(place: Place) {
        failures.check()
        this.place.value = place
    }

    override suspend fun searchPlaces(query: String): List<Place> {
        if (failSearches) throw IOException("simulated search failure")
        return searchResults
    }

    override val locationPermissionRequested: Flow<Boolean> = requested

    override suspend fun markLocationPermissionRequested() {
        requested.value = true
    }

    val walkLocation = MutableStateFlow<WalkLocation?>(null)

    override val activeWalkLocation: Flow<WalkLocation?> = walkLocation

    override suspend fun saveActiveWalkLocation(location: WalkLocation) {
        failures.check()
        walkLocation.value = location
    }

    override suspend fun clearActiveWalkLocation() {
        failures.check()
        walkLocation.value = null
    }
}
