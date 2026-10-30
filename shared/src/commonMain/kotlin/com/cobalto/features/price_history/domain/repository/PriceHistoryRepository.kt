package com.cobalto.features.price_history.domain.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.price_history.domain.model.PriceHistory
import com.cobalto.features.price_history.domain.model.PriceHistoryDetail
import kotlinx.coroutines.flow.Flow

interface PriceHistoryRepository {
    suspend fun refreshPriceHistory(): ResultState<Unit>
    suspend fun upsertPriceHistory(priceHistory: PriceHistory): ResultState<Unit>
    fun getPriceHistoryDetailFlow(): Flow<ResultState<List<PriceHistoryDetail>>>
    fun listenToRealtimeChanges(): Flow<Unit>
}
