package com.cobalto.features.price_history.data.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.database.resource.ResourceDao
import com.cobalto.core.database.price_history.PriceHistoryDao
import com.cobalto.features.price_history.data.mapper.toEntity
import com.cobalto.features.price_history.data.remote.PriceHistoryRemoteDataSource
import com.cobalto.features.price_history.domain.model.PriceHistory
import com.cobalto.features.price_history.domain.model.PriceHistoryDetail
import com.cobalto.features.price_history.domain.repository.PriceHistoryRepository
import com.cobalto.core.database.provider.ProviderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class PriceHistoryRepositoryImpl(
    private val resourceDao: ResourceDao,
    private val providerDao: ProviderDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val remoteDataSource: PriceHistoryRemoteDataSource
) : PriceHistoryRepository {

    override suspend fun refreshPriceHistory(): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            println("Starting refreshPriceHistory...")
            val remotePrices = remoteDataSource.getPriceHistories()
            println("Fetched ${remotePrices.size} prices")
            priceHistoryDao.insertPriceHistories(remotePrices.map { it.toEntity() })
            resourceDao.syncAllResourcesCurrentPricesFromHistory()
            println("refreshPriceHistory finished successfully")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            println("Error refreshing price history: ${e.message}")
            e.printStackTrace()
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun upsertPriceHistory(priceHistory: PriceHistory): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.upsertPriceHistory(priceHistory)
            priceHistoryDao.insertPriceHistory(priceHistory.toEntity())
            resourceDao.updateResourceCurrentPriceFromHistory(priceHistory.materialId)
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error al guardar historial de precios")
        }
    }

    override fun getPriceHistoryDetailFlow(): Flow<ResultState<List<PriceHistoryDetail>>> {
        return combine(
            resourceDao.getResources(),
            providerDao.getProviders(),
            priceHistoryDao.getAllPriceHistory()
        ) { materials, providers, prices ->
            prices.map { priceEntity ->
                val material = materials.find { it.resourceId == priceEntity.materialId }
                val provider = providers.find { it.providerId == priceEntity.providerId }
                PriceHistoryDetail(
                    historyId = priceEntity.historyId,
                    materialName = material?.name ?: "Desconocido",
                    providerName = provider?.name ?: "Desconocido",
                    price = priceEntity.price,
                    quoteDate = priceEntity.quoteDate,
                    unit = material?.unit
                )
            }.sortedByDescending { it.quoteDate }
        }
            .map { ResultState.Success(it) as ResultState<List<PriceHistoryDetail>> }
            .onStart { emit(ResultState.Loading) }
            .catch { e -> emit(ResultState.Error(e.message ?: "Unknown error")) }
    }

    override fun listenToRealtimeChanges(): Flow<Unit> = remoteDataSource.observePriceHistories().map {
        refreshPriceHistory()
    }
}
