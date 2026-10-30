package com.cobalto.features.db.domain.repository

import com.cobalto.features.db.domain.model.DbItem
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.db.presentation.ApuResourceItem
import kotlinx.coroutines.flow.Flow

interface DbRepository {
    fun getDbs(): Flow<List<DbItem>>
    suspend fun syncDbs()
    fun getDbItems(dbId: String): Flow<List<DbSubItem>>
    suspend fun syncDbItems(dbId: String)
    fun getApuResourcesForDbItem(itemId: String): Flow<List<ApuResourceItem>>
    suspend fun syncDbItemResources(itemId: String)
    suspend fun addResourceToDbItem(itemId: String, resourceId: String, quantity: Double)
}
