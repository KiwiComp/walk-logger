package com.example.walklogger.domain

import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.runCatchingCancellable
import com.example.walklogger.core.data.ActiveWalkLocationRepository
import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import javax.inject.Inject

/** Stores a finished walk, logging a new one or replacing a stored one. */
class SaveWalkUseCase @Inject constructor(
    private val walkRepository: WalkRepository,
    private val activeWalkLocationRepository: ActiveWalkLocationRepository,
    private val errorReporter: ErrorReporter,
) {
    /**
     * A [walk] whose id is [UNSAVED] is logged as a new walk. Any other walk replaces the stored one,
     * and the start position stored for it is no longer needed. Throws when the walk cannot be saved.
     */
    suspend operator fun invoke(walk: Walk) {
        if (walk.id == UNSAVED) {
            walkRepository.logWalk(walk)
        } else {
            walkRepository.updateWalk(walk)
            // A position left behind is harmless: no other walk has this id.
            runCatchingCancellable { activeWalkLocationRepository.clearFor(walk.id) }
                .onFailure { errorReporter.report(it, "Failed to clear walk location") }
        }
    }

    companion object {
        /** The id of a walk that has not been stored yet. */
        const val UNSAVED = 0L
    }
}
