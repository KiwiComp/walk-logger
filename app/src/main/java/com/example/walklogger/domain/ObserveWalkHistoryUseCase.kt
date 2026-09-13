package com.example.walklogger.domain

import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Example use case. The domain layer is optional; add use cases only when logic is
 * shared between ViewModels or is complex enough to deserve its own unit tests.
 */
class ObserveWalkHistoryUseCase @Inject constructor(
    private val walkRepository: WalkRepository,
) {
    /** Completed walks, newest first. */
    operator fun invoke(): Flow<List<Walk>> =
        walkRepository.observeWalks().map { walks -> walks.filter { !it.isActive } }
}
