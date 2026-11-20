package com.example.walklogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.UserSettingsRepository
import com.example.walklogger.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainActivityViewModel @Inject constructor(userSettings: UserSettingsRepository) :
    ViewModel() {
    /**
     * Null until the saved choice has loaded; MainActivity holds the first frame until then.
     * Eager, because the held frame means nothing collects it yet.
     */
    val themeMode: StateFlow<ThemeMode?> = userSettings.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )
}
