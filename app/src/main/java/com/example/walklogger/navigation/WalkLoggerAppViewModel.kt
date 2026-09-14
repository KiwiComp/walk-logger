package com.example.walklogger.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Reopens the timer when the app starts with a walk still in progress. Checked once per process:
 * a recreated activity restores its own back stack, so it must not be sent to the timer again.
 */
@HiltViewModel
class WalkLoggerAppViewModel @Inject constructor(walkRepository: WalkRepository) : ViewModel() {

    private val _walkToResume = MutableStateFlow<Long?>(null)

    /** Id of the in-progress walk found at startup, until the UI has navigated to it. */
    val walkToResume: StateFlow<Long?> = _walkToResume.asStateFlow()

    init {
        viewModelScope.launch {
            _walkToResume.value = walkRepository.observeActiveWalk().first()?.id
        }
    }

    fun onWalkResumed() {
        _walkToResume.value = null
    }
}
