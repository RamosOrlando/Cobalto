package com.cobalto.features.decimal_format.data.repository

import com.cobalto.core.database.decimal_format.DecimalFormatDao
import com.cobalto.features.decimal_format.data.mapper.toDomain
import com.cobalto.features.decimal_format.data.mapper.toEntity
import com.cobalto.features.decimal_format.data.remote.DecimalFormatRemoteDataSource
import com.cobalto.features.decimal_format.domain.model.DecimalFormat
import com.cobalto.features.decimal_format.domain.repository.DecimalFormatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DecimalFormatRepositoryImpl(
    private val decimalFormatDao: DecimalFormatDao,
    private val remoteDataSource: DecimalFormatRemoteDataSource
) : DecimalFormatRepository {

    override fun getDecimalFormatsFlow(): Flow<List<DecimalFormat>> {
        return decimalFormatDao.getAllDecimalFormats().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshDecimalFormats() = withContext(Dispatchers.IO) {
        runCatching {
            val remoteFormats = remoteDataSource.getDecimalFormats()
            if (remoteFormats.isNotEmpty()) {
                decimalFormatDao.insertDecimalFormats(remoteFormats.map { it.toEntity() })
            }
        }
        Unit
    }

    override suspend fun saveDecimalFormatsLocally(formats: List<DecimalFormat>) = withContext(Dispatchers.IO) {
        decimalFormatDao.insertDecimalFormats(formats.map { it.toEntity() })
    }
}
