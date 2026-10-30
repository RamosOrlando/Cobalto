package com.cobalto.features.maker.data.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.database.maker.MakerDao
import com.cobalto.features.maker.data.mapper.toDomain
import com.cobalto.features.maker.data.mapper.toEntity
import com.cobalto.features.maker.data.remote.MakerRemoteDataSource
import com.cobalto.features.maker.domain.model.Maker
import com.cobalto.features.maker.domain.repository.MakerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class MakerRepositoryImpl(
    private val makerDao: MakerDao,
    private val remoteDataSource: MakerRemoteDataSource
) : MakerRepository {

    override suspend fun refreshMakers(): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            println("Starting refreshMakers...")
            val remoteMakers = remoteDataSource.getMakers()
            println("Fetched ${remoteMakers.size} makers")
            makerDao.insertMakers(remoteMakers.map { it.toEntity() })
            println("refreshMakers finished successfully")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            println("Error refreshing makers: ${e.message}")
            e.printStackTrace()
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    override fun getMakersFlow(): Flow<ResultState<List<Maker>>> {
        return makerDao.getMakers()
            .map { entities ->
                val domainMakers = entities.map { it.toDomain() }
                ResultState.Success(domainMakers) as ResultState<List<Maker>>
            }
            .onStart { emit(ResultState.Loading) }
            .catch { e -> emit(ResultState.Error(e.message ?: "Unknown error")) }
    }

    override fun listenToRealtimeChanges(): Flow<Unit> = flow {
        remoteDataSource.observeMakers().collect {
            refreshMakers()
            emit(Unit)
        }
    }

    override suspend fun saveMaker(maker: Maker): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.saveMaker(maker)
            makerDao.insertMakers(listOf(maker.toEntity()))
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Unknown error")
        }
    }
}
