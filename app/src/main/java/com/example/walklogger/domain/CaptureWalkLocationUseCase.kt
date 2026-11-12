package com.example.walklogger.domain

import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.PlaceRepository
import com.example.walklogger.core.location.LocationProvider
import com.example.walklogger.core.model.WalkLocation
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Stores where an active walk is so [FetchWalkWeatherUseCase] can skip the slow location lookup when
 * the walk ends. Never asks for permission and never fails; without a position the fetch looks it up as usual.
 */
class CaptureWalkLocationUseCase @Inject constructor(
    private val locationProvider: LocationProvider,
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(walkId: Long) {
        if (!locationProvider.hasPermission()) return
        runCatchingCancellable {
            if (placeRepository.activeWalkLocation.first()?.walkId == walkId) return@runCatchingCancellable
            val coordinates = locationProvider.currentLocation() ?: return@runCatchingCancellable
            placeRepository.saveActiveWalkLocation(WalkLocation(walkId, coordinates))
        }
    }
}
