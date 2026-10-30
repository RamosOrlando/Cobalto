package com.cobalto.features.price_history.data.remote

import com.cobalto.features.price_history.domain.model.PriceHistory
import kotlinx.coroutines.flow.Flow

interface PriceHistoryRemoteDataSource {
    suspend fun getPriceHistories(): List<PriceHistory>
    suspend fun upsertPriceHistory(priceHistory: PriceHistory)
    fun observePriceHistories(): Flow<Unit>
}
