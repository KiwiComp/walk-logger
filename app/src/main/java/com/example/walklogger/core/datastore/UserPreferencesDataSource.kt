package com.example.walklogger.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Small user preferences that don't belong in a Room table. */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val activeDogId: Flow<Long?> = dataStore.data.map { it[ACTIVE_DOG_ID] }

    suspend fun setActiveDogId(id: Long) {
        dataStore.edit { it[ACTIVE_DOG_ID] = id }
    }

    suspend fun clearActiveDogId() {
        dataStore.edit { it.remove(ACTIVE_DOG_ID) }
    }

    /** The place weather is fetched for when the device location is unavailable. */
    val weatherPlace: Flow<Place?> = dataStore.data.map { prefs ->
        val name = prefs[WEATHER_PLACE_NAME] ?: return@map null
        val latitude = prefs[WEATHER_PLACE_LATITUDE] ?: return@map null
        val longitude = prefs[WEATHER_PLACE_LONGITUDE] ?: return@map null
        Place(name = name, region = prefs[WEATHER_PLACE_REGION], coordinates = Coordinates(latitude, longitude))
    }

    suspend fun setWeatherPlace(place: Place) {
        dataStore.edit {
            it[WEATHER_PLACE_NAME] = place.name
            if (place.region != null) it[WEATHER_PLACE_REGION] = place.region else it.remove(WEATHER_PLACE_REGION)
            it[WEATHER_PLACE_LATITUDE] = place.coordinates.latitude
            it[WEATHER_PLACE_LONGITUDE] = place.coordinates.longitude
        }
    }

    val locationPermissionRequested: Flow<Boolean> = dataStore.data.map { it[LOCATION_PERMISSION_REQUESTED] ?: false }

    suspend fun setLocationPermissionRequested() {
        dataStore.edit { it[LOCATION_PERMISSION_REQUESTED] = true }
    }

    val activeWalkLocation: Flow<WalkLocation?> = dataStore.data.map { prefs ->
        val walkId = prefs[ACTIVE_WALK_ID] ?: return@map null
        val latitude = prefs[ACTIVE_WALK_LATITUDE] ?: return@map null
        val longitude = prefs[ACTIVE_WALK_LONGITUDE] ?: return@map null
        WalkLocation(walkId, Coordinates(latitude, longitude))
    }

    suspend fun setActiveWalkLocation(location: WalkLocation) {
        dataStore.edit {
            it[ACTIVE_WALK_ID] = location.walkId
            it[ACTIVE_WALK_LATITUDE] = location.coordinates.latitude
            it[ACTIVE_WALK_LONGITUDE] = location.coordinates.longitude
        }
    }

    suspend fun clearActiveWalkLocation() {
        dataStore.edit {
            it.remove(ACTIVE_WALK_ID)
            it.remove(ACTIVE_WALK_LATITUDE)
            it.remove(ACTIVE_WALK_LONGITUDE)
        }
    }

    private companion object {
        val ACTIVE_DOG_ID = longPreferencesKey("active_dog_id")
        val WEATHER_PLACE_NAME = stringPreferencesKey("weather_place_name")
        val WEATHER_PLACE_REGION = stringPreferencesKey("weather_place_region")
        val WEATHER_PLACE_LATITUDE = doublePreferencesKey("weather_place_latitude")
        val WEATHER_PLACE_LONGITUDE = doublePreferencesKey("weather_place_longitude")
        val LOCATION_PERMISSION_REQUESTED = booleanPreferencesKey("location_permission_requested")
        val ACTIVE_WALK_ID = longPreferencesKey("active_walk_id")
        val ACTIVE_WALK_LATITUDE = doublePreferencesKey("active_walk_latitude")
        val ACTIVE_WALK_LONGITUDE = doublePreferencesKey("active_walk_longitude")
    }
}
