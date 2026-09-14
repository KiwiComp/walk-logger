package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.database.WalkDao
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Walk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Room-backed [WalkRepository]. */
@Singleton
class DefaultWalkRepository @Inject constructor(
    private val walkDao: WalkDao,
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
                    startTime = System.currentTimeMillis(),
                ),
            )
        }

    override suspend fun finishWalk(walkId: Long) = withContext(ioDispatcher) {
        val walk = walkDao.getWalk(walkId) ?: return@withContext
        walkDao.updateWalk(walk.copy(endTime = System.currentTimeMillis()))
    }

    override suspend fun deleteWalk(id: Long) = withContext(ioDispatcher) {
        walkDao.deleteWalk(id)
    }
}
