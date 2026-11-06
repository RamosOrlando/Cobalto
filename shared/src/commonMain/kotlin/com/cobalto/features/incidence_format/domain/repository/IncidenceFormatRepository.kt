package com.cobalto.features.incidence_format.domain.repository

import com.cobalto.features.incidence_format.domain.model.IncidenceFormat
import kotlinx.coroutines.flow.Flow

interface IncidenceFormatRepository {
    fun getIncidenceFormatsFlow(): Flow<List<IncidenceFormat>>
    suspend fun refreshIncidenceFormats()
}
