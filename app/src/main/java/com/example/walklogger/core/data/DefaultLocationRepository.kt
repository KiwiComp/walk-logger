package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.datastore.UserPreferencesDataSource
import com.example.walklogger.core.location.LocationProvider
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.network.OpenMeteoGeocodingApi
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DefaultLocationRepository @Inject constructor(
    private val locationProvider: LocationProvider,
    private val preferences: UserPreferencesDataSource,
    private val geocodingApi: OpenMeteoGeocodingApi,
    @Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : LocationRepository {

    override fun hasPermission(): Boolean = locationProvider.hasPermission()

    override suspend fun currentLocation(): Coordinates? = locationProvider.currentLocation()

    override val locationPermissionRequested: Flow<Boolean> =
        preferences.locationPermissionRequested

    override suspend fun markLocationPermissionRequested() =
        preferences.setLocationPermissionRequested()

    override val savedPlace: Flow<Place?> = preferences.weatherPlace

    override suspend fun savePlace(place: Place) = preferences.setWeatherPlace(place)

    override suspend fun searchPlaces(query: String): List<Place> = withContext(ioDispatcher) {
        geocodingApi.search(
            name = query.trim(),
            language = Locale.getDefault().language,
        ).results.map {
            Place(
                name = it.name,
                region = listOfNotNull(it.admin1, it.country).joinToString(", ").ifEmpty { null },
                coordinates = Coordinates(it.latitude, it.longitude),
            )
        }
    }
}
