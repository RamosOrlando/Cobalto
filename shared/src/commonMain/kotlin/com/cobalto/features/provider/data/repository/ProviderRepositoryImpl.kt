package com.cobalto.features.provider.data.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.database.provider.ProviderDao
import com.cobalto.features.provider.data.mapper.toDomain
import com.cobalto.features.provider.data.mapper.toEntity
import com.cobalto.features.provider.data.remote.ProviderRemoteDataSource
import com.cobalto.features.provider.domain.model.Provider
import com.cobalto.features.provider.domain.repository.ProviderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class ProviderRepositoryImpl(
    private val providerDao: ProviderDao,
    private val remoteDataSource: ProviderRemoteDataSource
) : ProviderRepository {

    override suspend fun refreshProviders(): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            println("Starting refreshProviders...")
            val remoteProviders = remoteDataSource.getProviders()
            println("Fetched ${remoteProviders.size} providers")
            providerDao.insertProviders(remoteProviders.map { it.toEntity() })
            println("refreshProviders finished successfully")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            println("Error refreshing providers: ${e.message}")
            e.printStackTrace()
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    override fun getProvidersFlow(): Flow<ResultState<List<Provider>>> {
        return providerDao.getProviders()
            .map { entities ->
                val domainProviders = entities.map { it.toDomain() }
                ResultState.Success(domainProviders) as ResultState<List<Provider>>
            }
            .onStart { emit(ResultState.Loading) }
            .catch { e -> emit(ResultState.Error(e.message ?: "Unknown error")) }
    }

    override fun listenToRealtimeChanges(): Flow<Unit> = flow {
        remoteDataSource.observeProviders().collect {
            refreshProviders()
            emit(Unit)
        }
    }

    override suspend fun saveProvider(provider: Provider): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.saveProvider(provider)
            providerDao.insertProviders(listOf(provider.toEntity()))
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Unknown error")
        }
    }
}
