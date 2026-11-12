package com.example.walklogger.domain

import com.example.walklogger.core.data.PlaceRepository
import com.example.walklogger.core.location.LocationProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Whether the app may, and still should, ask for location. It asks once, wherever that happens first. */
class LocationPermissionUseCase @Inject constructor(
    private val locationProvider: LocationProvider,
    private val placeRepository: PlaceRepository,
) {
    fun hasPermission(): Boolean = locationProvider.hasPermission()

    /** False once the app has asked; after that a denied permission falls back to the saved place. */
    suspend fun shouldRequest(): Boolean = !hasPermission() && !placeRepository.locationPermissionRequested.first()

    suspend fun markRequested() = placeRepository.markLocationPermissionRequested()
}
