package com.cobalto.features.incidence_format.data.remote

import com.cobalto.features.incidence_format.domain.model.IncidenceFormat

interface IncidenceFormatRemoteDataSource {
    suspend fun getIncidenceFormats(): List<IncidenceFormat>
}
