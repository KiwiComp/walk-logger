package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.database.BehaviourIssueDao
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import com.example.walklogger.core.model.BehaviourIssue
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Room-backed [BehaviourIssueRepository]. */
@Singleton
class DefaultBehaviourIssueRepository @Inject constructor(
    private val behaviourIssueDao: BehaviourIssueDao,
    @param:Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : BehaviourIssueRepository {

    override fun observeIssues(): Flow<List<BehaviourIssue>> =
        behaviourIssueDao.observeIssues().map { issues -> issues.map { it.toModel() } }

    override suspend fun addIssue(name: String): BehaviourIssue = withContext(ioDispatcher) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "issue name must not be blank" }
        val existing = behaviourIssueDao.observeIssues().first().find { it.name.equals(trimmed, ignoreCase = true) }
        existing?.toModel() ?: BehaviourIssue(id = behaviourIssueDao.insertIssue(BehaviourIssueEntity(name = trimmed)), name = trimmed)
    }
}
