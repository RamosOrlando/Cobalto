package com.cobalto.features.db.domain.repository

import com.cobalto.core.database.db_item_resource.DbItemResourceCounts
import com.cobalto.features.db.domain.model.DbItem
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.db.presentation.ApuResourceItem
import kotlinx.coroutines.flow.Flow

interface DbRepository {
    fun getDbs(): Flow<List<DbItem>>
    fun getItemCountsMap(): Flow<Map<String, Int>>
    suspend fun syncDbs()
    fun getDbItems(dbId: String): Flow<List<DbSubItem>>
    fun getAllDbItems(): Flow<List<DbSubItem>>
    suspend fun syncDbItems(dbId: String)
    fun getApuResourcesForDbItem(itemId: String): Flow<List<ApuResourceItem>>
    fun getResourceCountsMap(): Flow<Map<String, DbItemResourceCounts>>
    suspend fun syncDbItemResources(itemId: String)
    suspend fun addResourceToDbItem(itemId: String, resourceId: String, quantity: Double)
    suspend fun deleteResourceFromDbItem(itemId: String, resourceId: String)
    suspend fun addDbItem(dbId: String, name: String, code: String?, unit: String)
    suspend fun updateDbItemTotal(itemId: String, total: Double)
}
