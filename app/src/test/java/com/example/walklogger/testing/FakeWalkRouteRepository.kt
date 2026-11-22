package com.example.walklogger.testing

import com.example.walklogger.core.data.WalkRouteRepository
import com.example.walklogger.core.model.RoutePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [WalkRouteRepository] for unit tests; [points] holds every walk's points by walk id. */
class FakeWalkRouteRepository : WalkRouteRepository {

    val failures = FailureSwitch()

    val points = MutableStateFlow<Map<Long, List<RoutePoint>>>(emptyMap())

    override fun observeRoute(walkId: Long): Flow<List<RoutePoint>> =
        points.map { byWalk -> byWalk[walkId].orEmpty().sortedBy { it.time } }

    override suspend fun addPoint(walkId: Long, point: RoutePoint) {
        failures.check()
        points.update { it + (walkId to it[walkId].orEmpty() + point) }
    }
}
