package com.cobalto.features.decimal_format.data.remote

import com.cobalto.features.decimal_format.domain.model.DecimalFormat

interface DecimalFormatRemoteDataSource {
    suspend fun getDecimalFormats(): List<DecimalFormat>
}
