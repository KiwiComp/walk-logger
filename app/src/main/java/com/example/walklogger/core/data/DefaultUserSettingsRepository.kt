package com.example.walklogger.core.data

import com.example.walklogger.core.datastore.UserPreferencesDataSource
import com.example.walklogger.core.model.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DefaultUserSettingsRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
) : UserSettingsRepository {

    override val themeMode: Flow<ThemeMode> = preferences.themeMode

    override suspend fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)
}
