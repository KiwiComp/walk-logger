package com.example.walklogger.domain

import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.ActiveWalkLocationRepository
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.data.WalkRouteRepository
import com.example.walklogger.core.model.RoutePoint
import com.example.walklogger.core.model.WalkLocation
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Follows an active walk's position until the walk ends or is discarded: first the start position,
 * so [FetchWalkWeatherUseCase] can skip the slow location lookup when the walk ends, then every
 * route point. Never asks for permission and never fails; without precise location it does nothing.
 */
class TrackWalkLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val activeWalkLocationRepository: ActiveWalkLocationRepository,
    private val walkRepository: WalkRepository,
    private val walkRouteRepository: WalkRouteRepository,
    private val clock: Clock,
    private val errorReporter: ErrorReporter,
) {
    /** Suspends while the walk with [walkId] is active. */
    suspend operator fun invoke(walkId: Long) {
        if (!locationRepository.hasPermission()) return
        captureStartLocation(walkId)
        recordRoute(walkId)
    }

    private suspend fun captureStartLocation(walkId: Long) {
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

    private suspend fun recordRoute(walkId: Long) {
        runCatchingCancellable {
            coroutineScope {
                val recording = launch {
                    locationRepository.locationUpdates().collect { coordinates ->
                        // One lost point is better than a route that stops; the next update tries again.
                        runCatchingCancellable {
                            walkRouteRepository.addPoint(
                                walkId,
                                RoutePoint(clock.millis(), coordinates),
                            )
                        }.onFailure { errorReporter.report(it, "Failed to store route point") }
                    }
                }
                walkRepository.observeWalk(walkId).first { it?.isActive != true }
                recording.cancel()
            }
        }.onFailure { errorReporter.report(it, "Failed to record walk route") }
    }
}
