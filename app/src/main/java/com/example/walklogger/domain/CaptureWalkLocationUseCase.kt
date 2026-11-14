package com.example.walklogger.domain

import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.ActiveWalkLocationRepository
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.model.WalkLocation
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Stores where an active walk is so [FetchWalkWeatherUseCase] can skip the slow location lookup when
 * the walk ends. Never asks for permission and never fails; without a position the fetch looks it up as usual.
 */
class CaptureWalkLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val activeWalkLocationRepository: ActiveWalkLocationRepository,
    private val errorReporter: ErrorReporter,
) {
    suspend operator fun invoke(walkId: Long) {
        if (!locationRepository.hasPermission()) return
        runCatchingCancellable {
            if (activeWalkLocationRepository.activeWalkLocation.first()?.walkId ==
                walkId
            ) {
                return@runCatchingCancellable
            }
            val coordinates = locationRepository.currentLocation() ?: return@runCatchingCancellable
            activeWalkLocationRepository.save(WalkLocation(walkId, coordinates))
        }.onFailure { errorReporter.report(it, "Failed to capture walk location") }
    }
}
