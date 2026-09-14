package com.example.walklogger.feature.logwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.HandlerRepository
import com.example.walklogger.core.model.Equipment
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** [date] is the day chosen on the log screen; passed in directly so tests need no SavedStateHandle. */
@HiltViewModel(assistedFactory = NewWalkViewModel.Factory::class)
class NewWalkViewModel @AssistedInject constructor(
    @Assisted private val date: LocalDate,
    dogRepository: DogRepository,
    private val handlerRepository: HandlerRepository,
    clock: Clock,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(date: LocalDate): NewWalkViewModel
    }

    private val isToday = date == LocalDate.now(clock)

    private val selectedHandlers = MutableStateFlow<Set<String>>(emptySet())
    private val equipment = MutableStateFlow(Equipment.HARNESS)

    val uiState: StateFlow<NewWalkUiState> =
        combine(
            dogRepository.observeActiveDog(),
            handlerRepository.observeHandlers(),
            selectedHandlers,
            equipment,
        ) { dog, handlers, selected, equipment ->
            NewWalkUiState(
                date = date,
                isToday = isToday,
                dogName = dog?.name,
                handlers = handlers,
                selectedHandlers = selected,
                equipment = equipment,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewWalkUiState(date = date, isToday = isToday),
        )

    fun toggleHandler(handler: String) = selectedHandlers.update { selected ->
        if (handler in selected) selected - handler else selected + handler
    }

    /** Stores the name (or finds the existing one) and selects it. */
    fun addHandler(name: String) {
        viewModelScope.launch {
            val stored = handlerRepository.addHandler(name)
            selectedHandlers.update { it + stored }
        }
    }

    fun selectEquipment(value: Equipment) = equipment.update { value }
}
