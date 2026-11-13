package com.example.walklogger.core.data

import com.example.walklogger.core.datastore.UserPreferencesDataSource
import com.example.walklogger.core.model.WalkLocation
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DefaultActiveWalkLocationRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
) : ActiveWalkLocationRepository {

    override val activeWalkLocation: Flow<WalkLocation?> = preferences.activeWalkLocation

    override suspend fun save(location: WalkLocation) = preferences.setActiveWalkLocation(location)

    override suspend fun clear() = preferences.clearActiveWalkLocation()
}
