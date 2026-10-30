package com.cobalto.features.category.data.remote

import com.cobalto.features.category.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRemoteDataSource {
    suspend fun getCategories(): List<Category>
    fun observeCategories(): Flow<Unit>
}
