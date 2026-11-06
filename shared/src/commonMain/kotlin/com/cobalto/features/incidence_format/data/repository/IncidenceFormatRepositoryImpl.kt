package com.cobalto.features.incidence_format.data.repository

import com.cobalto.core.database.incidence_format.IncidenceFormatDao
import com.cobalto.features.incidence_format.data.mapper.toDomain
import com.cobalto.features.incidence_format.data.mapper.toEntity
import com.cobalto.features.incidence_format.data.remote.IncidenceFormatRemoteDataSource
import com.cobalto.features.incidence_format.domain.model.IncidenceFormat
import com.cobalto.features.incidence_format.domain.repository.IncidenceFormatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IncidenceFormatRepositoryImpl(
    private val incidenceFormatDao: IncidenceFormatDao,
    private val remoteDataSource: IncidenceFormatRemoteDataSource
) : IncidenceFormatRepository {

    override fun getIncidenceFormatsFlow(): Flow<List<IncidenceFormat>> {
        return incidenceFormatDao.getAllIncidenceFormats().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshIncidenceFormats() = withContext(Dispatchers.IO) {
        runCatching {
            val remoteFormats = remoteDataSource.getIncidenceFormats()
            if (remoteFormats.isNotEmpty()) {
                incidenceFormatDao.insertIncidenceFormats(remoteFormats.map { it.toEntity() })
            }
        }
        Unit
    }
}
