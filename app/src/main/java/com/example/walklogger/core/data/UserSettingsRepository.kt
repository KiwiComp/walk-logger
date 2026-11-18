package com.example.walklogger.core.data

import com.example.walklogger.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** App-wide choices the user makes in Settings. */
interface UserSettingsRepository {
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
