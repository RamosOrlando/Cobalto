package com.cobalto.features.db.data.remote

import com.cobalto.features.db.domain.model.DbItem
import com.cobalto.features.db.domain.model.DbItemResource
import com.cobalto.features.db.domain.model.DbSubItem
import kotlinx.coroutines.flow.Flow

interface DbRemoteDataSource {
    suspend fun getDbs(): List<DbItem>
    fun observeDbs(): Flow<Unit>
    suspend fun getDbItems(dbId: String): List<DbSubItem>
    suspend fun getDbItemResources(itemId: String): List<DbItemResource>
    suspend fun insertDbItemResource(itemResource: DbItemResource)
    suspend fun deleteDbItemResource(itemId: String, resourceId: String)
    suspend fun insertDbItem(dbItem: DbSubItem)
    suspend fun updateDbItemTotal(itemId: String, total: Double)
}
