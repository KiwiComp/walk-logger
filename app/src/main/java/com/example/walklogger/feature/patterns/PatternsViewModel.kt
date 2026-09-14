package com.example.walklogger.feature.patterns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PatternsViewModel @Inject constructor(dogRepository: DogRepository) : ViewModel() {

    val uiState: StateFlow<PatternsUiState> =
        dogRepository.observeActiveDog()
            .map { dog -> PatternsUiState(activeDogName = dog?.name) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PatternsUiState(),
            )
}
