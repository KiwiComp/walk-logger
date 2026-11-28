package com.example.walklogger.core.data

import com.example.walklogger.core.model.RoutePoint
import kotlinx.coroutines.flow.Flow

/** The positions recorded during active walks; kept apart from [WalkRepository] so lists of walks stay light. */
interface WalkRouteRepository {
    /** Every point of the walk with [walkId], oldest first; empty for a walk logged afterwards. */
    fun observeRoute(walkId: Long): Flow<List<RoutePoint>>

    /** The points of the walks with [walkIds] by walk id, each oldest first; walks without a route are left out. */
    fun observeRoutes(walkIds: List<Long>): Flow<Map<Long, List<RoutePoint>>>

    suspend fun addPoint(walkId: Long, point: RoutePoint)
}
