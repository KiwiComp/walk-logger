package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.database.HandlerDao
import com.example.walklogger.core.database.entity.HandlerEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** Room-backed [HandlerRepository]. */
@Singleton
class DefaultHandlerRepository @Inject constructor(
    private val handlerDao: HandlerDao,
    @param:Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : HandlerRepository {

    override fun observeHandlers(): Flow<List<String>> = handlerDao.observeHandlerNames()

    override suspend fun addHandler(name: String): String = withContext(ioDispatcher) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "handler name must not be blank" }
        val existing = handlerDao.observeHandlerNames().first().find { it.equals(trimmed, ignoreCase = true) }
        existing ?: trimmed.also { handlerDao.insertHandler(HandlerEntity(name = it)) }
    }
}
