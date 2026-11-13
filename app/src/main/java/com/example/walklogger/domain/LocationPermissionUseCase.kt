package com.example.walklogger.domain

import com.example.walklogger.core.data.LocationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Whether the app may, and still should, ask for location. It asks once, wherever that happens first. */
class LocationPermissionUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    fun hasPermission(): Boolean = locationRepository.hasPermission()

    /** False once the app has asked; after that a denied permission falls back to the saved place. */
    suspend fun shouldRequest(): Boolean =
        !hasPermission() && !locationRepository.locationPermissionRequested.first()

    suspend fun markRequested() = locationRepository.markLocationPermissionRequested()
}
