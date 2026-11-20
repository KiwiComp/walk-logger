package com.example.walklogger.testing

import com.example.walklogger.core.data.UserSettingsRepository
import com.example.walklogger.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserSettingsRepository(themeMode: ThemeMode = ThemeMode.System) : UserSettingsRepository {
    val failures = FailureSwitch()

    private val mode = MutableStateFlow(themeMode)

    override val themeMode: Flow<ThemeMode> = mode

    override suspend fun setThemeMode(mode: ThemeMode) {
        failures.check()
        this.mode.value = mode
    }
}
