package com.cobalto.features.decimal_format.domain.repository

import com.cobalto.features.decimal_format.domain.model.DecimalFormat
import kotlinx.coroutines.flow.Flow

interface DecimalFormatRepository {
    fun getDecimalFormatsFlow(): Flow<List<DecimalFormat>>
    suspend fun refreshDecimalFormats()
    suspend fun saveDecimalFormatsLocally(formats: List<DecimalFormat>)
}
