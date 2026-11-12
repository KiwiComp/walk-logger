package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.datastore.UserPreferencesDataSource
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.core.network.OpenMeteoGeocodingApi
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DefaultPlaceRepository @Inject constructor(
    private val api: OpenMeteoGeocodingApi,
    private val preferences: UserPreferencesDataSource,
    @Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : PlaceRepository {

    override val savedPlace: Flow<Place?> = preferences.weatherPlace

    override suspend fun savePlace(place: Place) = preferences.setWeatherPlace(place)

    override suspend fun searchPlaces(query: String): List<Place> = withContext(ioDispatcher) {
        api.search(name = query.trim(), language = Locale.getDefault().language).results.map {
            Place(
                name = it.name,
                region = listOfNotNull(it.admin1, it.country).joinToString(", ").ifEmpty { null },
                coordinates = Coordinates(it.latitude, it.longitude),
            )
        }
    }

    override val locationPermissionRequested: Flow<Boolean> =
        preferences.locationPermissionRequested

    override suspend fun markLocationPermissionRequested() =
        preferences.setLocationPermissionRequested()

    override val activeWalkLocation: Flow<WalkLocation?> = preferences.activeWalkLocation

    override suspend fun saveActiveWalkLocation(location: WalkLocation) =
        preferences.setActiveWalkLocation(location)

    override suspend fun clearActiveWalkLocation() = preferences.clearActiveWalkLocation()
}
