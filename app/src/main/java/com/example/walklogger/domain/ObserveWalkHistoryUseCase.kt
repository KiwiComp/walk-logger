package com.example.walklogger.domain

import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveWalkHistoryUseCase @Inject constructor(
    private val walkRepository: WalkRepository,
    private val dogRepository: DogRepository,
) {
    /** The active dog's completed walks, newest first; empty without an active dog. */
    operator fun invoke(): Flow<List<Walk>> =
        combine(walkRepository.observeWalks(), dogRepository.observeActiveDog()) {
                walks,
                dog,
            ->
            walks.filter { !it.isActive && it.dogId == dog?.id }
        }
}
