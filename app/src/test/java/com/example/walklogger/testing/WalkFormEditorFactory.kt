package com.example.walklogger.testing

import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.domain.FetchWalkWeatherUseCase
import com.example.walklogger.domain.LocationPermissionUseCase
import com.example.walklogger.feature.logwalk.WalkFormEditor
import kotlinx.coroutines.CoroutineScope

/** The [WalkFormEditor.Factory] Hilt generates, for ViewModel tests that build their ViewModel by hand. */
fun walkFormEditorFactory(
    behaviourIssueRepository: BehaviourIssueRepository,
    medicalConcernRepository: MedicalConcernRepository,
    errorReporter: ErrorReporter,
    fetchWalkWeather: FetchWalkWeatherUseCase,
    locationPermission: LocationPermissionUseCase,
    locationRepository: LocationRepository,
): WalkFormEditor.Factory = object : WalkFormEditor.Factory {
    override fun create(
        scope: CoroutineScope,
        showError: (Throwable, Int, String) -> Unit,
        walkId: Long?,
    ) = WalkFormEditor(
        scope, behaviourIssueRepository, medicalConcernRepository, showError,
        errorReporter, fetchWalkWeather, locationPermission, locationRepository, walkId,
    )
}
