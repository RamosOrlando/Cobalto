package com.cobalto.features.category.domain.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.category.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun refreshCategories(): ResultState<Unit>
    fun getCategoriesFlow(): Flow<ResultState<List<Category>>>
    fun listenToRealtimeChanges(): Flow<Unit>
}
