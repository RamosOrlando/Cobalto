package com.cobalto.features.provider.data.remote

import com.cobalto.features.provider.domain.model.Provider
import kotlinx.coroutines.flow.Flow

interface ProviderRemoteDataSource {
    suspend fun getProviders(): List<Provider>
    fun observeProviders(): Flow<Unit>
    suspend fun saveProvider(provider: Provider)
}
