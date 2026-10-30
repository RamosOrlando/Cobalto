package com.cobalto.features.resource.data.remote

import com.cobalto.core.common.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface ResourceRemoteDataSource {
    suspend fun getResources(): List<Resource>
    suspend fun updateResource(resource: Resource)
    suspend fun deleteResource(resourceId: String)
    fun observeResources(): Flow<Unit>
}
