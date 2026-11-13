package com.example.walklogger.testing

import com.example.walklogger.core.data.ActiveWalkLocationRepository
import com.example.walklogger.core.model.WalkLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [ActiveWalkLocationRepository] for unit tests; [walkLocation] is the stored position. */
class FakeActiveWalkLocationRepository : ActiveWalkLocationRepository {

    val failures = FailureSwitch()

    val walkLocation = MutableStateFlow<WalkLocation?>(null)

    override val activeWalkLocation: Flow<WalkLocation?> = walkLocation

    override suspend fun save(location: WalkLocation) {
        failures.check()
        walkLocation.value = location
    }

    override suspend fun clear() {
        failures.check()
        walkLocation.value = null
    }
}
