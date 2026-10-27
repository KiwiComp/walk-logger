package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.database.WalkDao
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.Walk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** Room-backed [WalkRepository]. */
@Singleton
class DefaultWalkRepository @Inject constructor(
    private val walkDao: WalkDao,
    private val clock: Clock,
    @param:Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : WalkRepository {

    override fun observeWalks(): Flow<List<Walk>> =
        walkDao.observeWalks().map { walks -> walks.map { it.toModel() } }

    override fun observeWalk(id: Long): Flow<Walk?> =
        walkDao.observeWalk(id).map { it?.toModel() }

    override fun observeActiveWalk(): Flow<Walk?> =
        walkDao.observeActiveWalk().map { it?.toModel() }

    override suspend fun startWalk(dogId: Long, handlers: List<String>, equipment: Equipment): Long =
        withContext(ioDispatcher) {
            walkDao.insertWalk(
                WalkEntity(
                    dogId = dogId,
                    handlers = handlers,
                    equipment = equipment,
                    startTime = clock.millis(),
                ),
            )
        }

    override suspend fun logWalk(walk: Walk): Long {
        require(!walk.isActive) { "a logged walk needs an end time" }
        return withContext(ioDispatcher) { walkDao.insertWalk(walk.copy(id = 0).toEntity(), walk.behaviourIssues.map { it.id }, walk.medicalConcerns.map { it.id }) }
    }

    override suspend fun updateWalk(walk: Walk) {
        require(!walk.isActive) { "a logged walk needs an end time" }
        withContext(ioDispatcher) { walkDao.updateWalk(walk.toEntity(), walk.behaviourIssues.map { it.id }, walk.medicalConcerns.map { it.id }) }
    }

    override suspend fun updateNotes(walkId: Long, notes: String) = withContext(ioDispatcher) {
        walkDao.updateNotes(walkId, notes)
    }

    override suspend fun updateStool(walkId: Long, stool: Stool?) = withContext(ioDispatcher) {
        walkDao.updateStool(walkId, stool?.passed, stool?.quantity, stool?.texture, stool?.colour)
    }

    override suspend fun finishWalk(walkId: Long) = withContext(ioDispatcher) {
        val walk = walkDao.getWalk(walkId) ?: return@withContext
        walkDao.updateWalk(walk.copy(endTime = clock.millis()))
    }

    override suspend fun deleteWalk(id: Long) = withContext(ioDispatcher) {
        walkDao.deleteWalk(id)
    }
}
