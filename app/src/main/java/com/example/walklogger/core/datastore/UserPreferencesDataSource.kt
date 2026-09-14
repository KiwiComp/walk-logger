package com.example.walklogger.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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

    private companion object {
        val ACTIVE_DOG_ID = longPreferencesKey("active_dog_id")
    }
}
