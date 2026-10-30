package com.cobalto.features.resource.domain.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.common.domain.model.Resource
import com.cobalto.features.resource.domain.model.ResourceWithPrices
import kotlinx.coroutines.flow.Flow

interface ResourceRepository {
    suspend fun refreshResources(): ResultState<Unit>
    fun getResourcesFlow(query: String = "", sectionId: String? = null): Flow<ResultState<List<ResourceWithPrices>>>
    fun getResourcesOnlyFlow(query: String = "", sectionId: String? = null): Flow<ResultState<List<Resource>>>
    fun getAllResourcesFlow(): Flow<ResultState<List<Resource>>>
    suspend fun updateResource(resource: Resource): ResultState<Unit>
    suspend fun deleteResource(resourceId: String): ResultState<Unit>
    suspend fun getResourceCount(sectionId: String): Int
    fun getResourceCountFlow(sectionId: String): Flow<Int>
    fun listenToRealtimeChanges(): Flow<Unit>
}
