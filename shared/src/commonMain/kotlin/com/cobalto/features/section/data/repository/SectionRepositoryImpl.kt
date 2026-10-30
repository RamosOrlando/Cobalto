package com.cobalto.features.section.data.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.database.section.SectionDao
import com.cobalto.features.section.data.mapper.toDomain
import com.cobalto.features.section.data.mapper.toEntity
import com.cobalto.features.section.data.remote.SectionRemoteDataSource
import com.cobalto.features.section.domain.model.Section
import com.cobalto.features.section.domain.repository.SectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class SectionRepositoryImpl(
    private val sectionDao: SectionDao,
    private val remoteDataSource: SectionRemoteDataSource
) : SectionRepository {

    override suspend fun refreshSections(): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            println("Starting refreshSections...")
            val remoteSections = remoteDataSource.getSections()
            println("Fetched ${remoteSections.size} sections")
            sectionDao.insertSections(remoteSections.map { it.toEntity() })
            println("refreshSections finished successfully")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            println("Error refreshing sections: ${e.message}")
            e.printStackTrace()
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    override fun getSectionsFlow(): Flow<ResultState<List<Section>>> {
        return sectionDao.getSections()
            .map { entities ->
                val domainSections = entities.map { it.toDomain() }
                ResultState.Success(domainSections) as ResultState<List<Section>>
            }
            .onStart { emit(ResultState.Loading) }
            .catch { e -> emit(ResultState.Error(e.message ?: "Unknown error")) }
    }

    override fun listenToRealtimeChanges(): Flow<Unit> = flow {
        remoteDataSource.observeSections().collect {
            refreshSections()
            emit(Unit)
        }
    }
}
