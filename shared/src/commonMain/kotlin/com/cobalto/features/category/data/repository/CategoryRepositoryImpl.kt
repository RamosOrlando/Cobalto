package com.cobalto.features.category.data.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.database.category.CategoryDao
import com.cobalto.features.category.data.mapper.toDomain
import com.cobalto.features.category.data.mapper.toEntity
import com.cobalto.features.category.data.remote.CategoryRemoteDataSource
import com.cobalto.features.category.domain.model.Category
import com.cobalto.features.category.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val remoteDataSource: CategoryRemoteDataSource
) : CategoryRepository {

    override suspend fun refreshCategories(): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            println("DEBUG: Starting refreshCategories from Supabase...")
            val remoteCategories = remoteDataSource.getCategories()
            println("DEBUG: Categories fetched from Supabase: ${remoteCategories.size}")
            
            remoteCategories.forEach { cat ->
                println("DEBUG: Category: id=${cat.categoryId}, name=${cat.name}, desc=${cat.description}")
            }

            categoryDao.insertCategories(remoteCategories.map { it.toEntity() })
            println("DEBUG: Categories successfully inserted into Room")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            println("DEBUG: ERROR in refreshCategories: ${e.message}")
            e.printStackTrace()
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    override fun getCategoriesFlow(): Flow<ResultState<List<Category>>> {
        return categoryDao.getCategories()
            .map { entities ->
                val domainCategories = entities.map { it.toDomain() }
                ResultState.Success(domainCategories) as ResultState<List<Category>>
            }
            .catch { e -> emit(ResultState.Error(e.message ?: "Unknown error")) }
    }

    override fun listenToRealtimeChanges(): Flow<Unit> = flow {
        remoteDataSource.observeCategories().collect {
            refreshCategories()
            emit(Unit)
        }
    }
}
