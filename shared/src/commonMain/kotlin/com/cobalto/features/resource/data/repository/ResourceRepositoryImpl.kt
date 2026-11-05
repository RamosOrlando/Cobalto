package com.cobalto.features.resource.data.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.database.resource.ResourceDao
import com.cobalto.core.database.price_history.PriceHistoryDao
import com.cobalto.features.resource.data.mapper.toDomain
import com.cobalto.features.resource.data.mapper.toEntity
import com.cobalto.features.resource.data.remote.ResourceRemoteDataSource
import com.cobalto.features.price_history.data.mapper.toDomain
import com.cobalto.features.resource.domain.model.ResourceWithPrices
import com.cobalto.features.resource.domain.model.PriceWithProvider
import com.cobalto.features.resource.domain.repository.ResourceRepository
import com.cobalto.core.database.provider.ProviderDao
import com.cobalto.features.provider.data.mapper.toDomain
import com.cobalto.core.database.maker.MakerDao
import com.cobalto.features.maker.data.mapper.toDomain
import com.cobalto.features.maker.data.mapper.toEntity as toMakerEntity
import com.cobalto.features.maker.data.remote.MakerRemoteDataSource
import com.cobalto.features.maker.domain.model.Maker
import com.cobalto.core.common.domain.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class ResourceRepositoryImpl(
    private val resourceDao: ResourceDao,
    private val providerDao: ProviderDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val makerDao: MakerDao,
    private val makerRemoteDataSource: MakerRemoteDataSource,
    private val remoteDataSource: ResourceRemoteDataSource
) : ResourceRepository {

    override suspend fun refreshResources(): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            println("Starting refreshMaterials...")
            runCatching {
                val remoteMakers = makerRemoteDataSource.getMakers()
                makerDao.insertMakers(remoteMakers.map { it.toMakerEntity() })
            }

            val remoteMaterials = remoteDataSource.getResources()
            println("Fetched ${remoteMaterials.size} materials")
            resourceDao.insertResources(remoteMaterials.map { it.toEntity() })
            resourceDao.syncAllResourcesCurrentPricesFromHistory()

            println("refreshMaterials finished successfully")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            println("Error refreshing materials: ${e.message}")
            e.printStackTrace()
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    override fun getResourcesFlow(query: String, sectionId: String?): Flow<ResultState<List<ResourceWithPrices>>> {
        return combine(
            resourceDao.getResourcesFiltered(query, sectionId),
            providerDao.getProviders(),
            priceHistoryDao.getAllPriceHistory(),
            makerDao.getMakers()
        ) { materials, providers, prices, makers ->
            val pricesByMaterialId = prices.groupBy { it.materialId }
            val providersById = providers.associateBy { it.providerId }
            val makersById = makers.associateBy { it.makerId }

            materials.map { resourceEntity ->
                val materialPrices = pricesByMaterialId[resourceEntity.resourceId]?.map { priceEntity ->
                    PriceWithProvider(
                        priceHistory = priceEntity.toDomain(),
                        provider = providersById[priceEntity.providerId]?.toDomain()
                    )
                } ?: emptyList()

                val resolvedMaker = makersById[resourceEntity.makerId]?.toDomain()
                    ?: Maker(resourceEntity.makerId, resourceEntity.makerId)

                ResourceWithPrices(
                    resource = resourceEntity.toDomain(),
                    maker = resolvedMaker,
                    prices = materialPrices
                )
            }
        }
            .map { ResultState.Success(it) as ResultState<List<ResourceWithPrices>> }
            .onStart { emit(ResultState.Loading) }
            .catch { e -> emit(ResultState.Error(e.message ?: "Unknown error")) }
            .flowOn(Dispatchers.Default)
    }

    override fun getResourcesOnlyFlow(query: String, sectionId: String?): Flow<ResultState<List<Resource>>> {
        return resourceDao.getResourcesFiltered(query, sectionId)
            .map { entities ->
                val materials = entities.map { it.toDomain() }
                ResultState.Success(materials) as ResultState<List<Resource>>
            }
            .onStart { emit(ResultState.Loading) }
            .catch { e -> emit(ResultState.Error(e.message ?: "Unknown error")) }
            .flowOn(Dispatchers.IO)
    }

    override fun getAllResourcesFlow(): Flow<ResultState<List<Resource>>> {
        return resourceDao.getResources()
            .map { entities ->
                val materials = entities.map { it.toDomain() }
                ResultState.Success(materials) as ResultState<List<Resource>>
            }
            .onStart { emit(ResultState.Loading) }
            .catch { e -> emit(ResultState.Error(e.message ?: "Unknown error")) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun updateResource(resource: Resource): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.updateResource(resource)
            resourceDao.insertResource(resource.toEntity())
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error al actualizar material")
        }
    }

    override suspend fun deleteResource(resourceId: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.deleteResource(resourceId)
            resourceDao.deleteResource(resourceId)
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error al eliminar material")
        }
    }

    override suspend fun getResourceCount(sectionId: String): Int = withContext(Dispatchers.IO) {
        resourceDao.getResourceCount(sectionId)
    }

    override fun getResourceCountFlow(sectionId: String): Flow<Int> {
        return resourceDao.getResourceCountFlow(sectionId)
    }

    override fun listenToRealtimeChanges(): Flow<Unit> = remoteDataSource.observeResources().map {
        refreshResources()
        Unit
    }
}
