package com.example.walklogger.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.domain.LocationPermissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val locationPermission: LocationPermissionUseCase,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val hasLocationPermission = MutableStateFlow(locationPermission.hasPermission())
    private val shouldRequestLocationPermission = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> =
        combine(
            hasLocationPermission,
            locationPermission.locationPermissionRequested,
            shouldRequestLocationPermission,
        ) { hasPermission, wasRequested, shouldRequest ->
            SettingsUiState(
                hasLocationPermission = hasPermission,
                wasLocationRequested = wasRequested,
                shouldRequestLocationPermission = shouldRequest,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(hasLocationPermission = hasLocationPermission.value),
        )

    /** The permission can change in system settings while the app is in the background. */
    fun refreshLocationPermission() = hasLocationPermission.update {
        locationPermission.hasPermission()
    }

    fun requestLocationPermission() = shouldRequestLocationPermission.update { true }

    fun onLocationPermissionResult(granted: Boolean) {
        shouldRequestLocationPermission.update { false }
        hasLocationPermission.update { granted }
        viewModelScope.launch {
            runCatchingCancellable { locationPermission.markRequested() }
                .onFailure {
                    errorReporter.report(it, "Failed to mark location permission requested")
                }
        }
    }
}
