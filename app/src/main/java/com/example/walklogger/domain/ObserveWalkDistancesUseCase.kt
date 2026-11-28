package com.example.walklogger.domain

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.data.WalkRouteRepository
import com.example.walklogger.core.model.Walk
import com.example.walklogger.core.model.distanceMeters
import com.example.walklogger.core.model.within
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveWalkDistancesUseCase @Inject constructor(
    private val walkRouteRepository: WalkRouteRepository,
    @param:Dispatcher(WalkLoggerDispatchers.Default) private val defaultDispatcher:
    CoroutineDispatcher,
) {
    /** Metres walked by walk id for the completed [walks], within each walk's times; walks without a route are left out. */
    operator fun invoke(walks: List<Walk>): Flow<Map<Long, Double>> {
        val completed = walks.filter { !it.isActive }
        if (completed.isEmpty()) return flowOf(emptyMap())
        return walkRouteRepository.observeRoutes(completed.map { it.id }).map { routes ->
            completed.mapNotNull { walk ->
                routes[walk.id]
                    ?.within(walk.startTime, walk.endTime)
                    ?.map { it.coordinates }
                    ?.distanceMeters()
                    ?.let { walk.id to it }
            }.toMap()
        }.flowOn(defaultDispatcher)
    }
}
