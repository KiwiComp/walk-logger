package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.database.MedicalConcernDao
import com.example.walklogger.core.database.entity.MedicalConcernEntity
import com.example.walklogger.core.model.MedicalConcern
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Room-backed [MedicalConcernRepository]. */
@Singleton
class DefaultMedicalConcernRepository @Inject constructor(
    private val medicalConcernDao: MedicalConcernDao,
    @param:Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : MedicalConcernRepository {

    override fun observeMedicalConcerns(): Flow<List<MedicalConcern>> =
        medicalConcernDao.observeMedicalConcerns().map { concerns -> concerns.map { it.toModel() } }

    override suspend fun addMedicalConcern(name: String): MedicalConcern = withContext(ioDispatcher) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "concern name must not be blank" }
        val existing = medicalConcernDao.findByName(trimmed)
        when {
            existing == null -> MedicalConcern(id = medicalConcernDao.insertMedicalConcern(MedicalConcernEntity(name = trimmed)), name = trimmed)
            existing.isHidden -> existing.toModel().also { medicalConcernDao.setHidden(existing.id, false) }
            else -> existing.toModel()
        }
    }
}
