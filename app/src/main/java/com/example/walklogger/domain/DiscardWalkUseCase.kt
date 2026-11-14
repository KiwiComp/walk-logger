package com.example.walklogger.domain

import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.ActiveWalkLocationRepository
import com.example.walklogger.core.data.WalkRepository
import javax.inject.Inject

/** Deletes a walk outright, together with the start position stored for it. */
class DiscardWalkUseCase @Inject constructor(
    private val walkRepository: WalkRepository,
    private val activeWalkLocationRepository: ActiveWalkLocationRepository,
    private val errorReporter: ErrorReporter,
) {
    /** Throws when the walk cannot be deleted. */
    suspend operator fun invoke(walkId: Long) {
        walkRepository.deleteWalk(walkId)
        // A position left behind is harmless: no other walk has this id.
        runCatchingCancellable { activeWalkLocationRepository.clearFor(walkId) }
            .onFailure { errorReporter.report(it, "Failed to clear walk location") }
    }
}
