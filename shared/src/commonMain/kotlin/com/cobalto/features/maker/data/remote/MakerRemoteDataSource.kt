package com.cobalto.features.maker.data.remote

import com.cobalto.features.maker.domain.model.Maker
import kotlinx.coroutines.flow.Flow

interface MakerRemoteDataSource {
    suspend fun getMakers(): List<Maker>
    fun observeMakers(): Flow<Unit>
    suspend fun saveMaker(maker: Maker)
}
