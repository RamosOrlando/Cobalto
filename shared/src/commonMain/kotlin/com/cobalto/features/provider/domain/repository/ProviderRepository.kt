package com.cobalto.features.provider.domain.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.provider.domain.model.Provider
import kotlinx.coroutines.flow.Flow

interface ProviderRepository {
    suspend fun refreshProviders(): ResultState<Unit>
    fun getProvidersFlow(): Flow<ResultState<List<Provider>>>
    fun listenToRealtimeChanges(): Flow<Unit>
    suspend fun saveProvider(provider: Provider): ResultState<Unit>
}
