package com.cobalto.features.db.data.repository

import com.cobalto.core.database.db.DbDao
import com.cobalto.core.database.db_item.DbItemDao
import com.cobalto.core.database.db_item_resource.DbItemResourceDao
import com.cobalto.features.db.data.mapper.toApuResourceItem
import com.cobalto.features.db.data.mapper.toDomain
import com.cobalto.features.db.data.mapper.toEntity
import com.cobalto.features.db.data.remote.DbRemoteDataSource
import com.cobalto.features.db.domain.model.DbItem
import com.cobalto.features.db.domain.model.DbItemResource
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.db.domain.repository.DbRepository
import com.cobalto.features.db.presentation.ApuResourceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DbRepositoryImpl(
    private val dbDao: DbDao,
    private val dbItemDao: DbItemDao,
    private val dbItemResourceDao: DbItemResourceDao,
    private val remoteDataSource: DbRemoteDataSource
) : DbRepository {

    override fun getDbs(): Flow<List<DbItem>> {
        return dbDao.getAllDbs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncDbs() {
        runCatching {
            val remoteDbs = remoteDataSource.getDbs()
            dbDao.insertDbs(remoteDbs.map { it.toEntity() })
        }
    }

    override fun getDbItems(dbId: String): Flow<List<DbSubItem>> {
        return dbItemDao.getItemsForDb(dbId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncDbItems(dbId: String) {
        runCatching {
            val remoteItems = remoteDataSource.getDbItems(dbId)
            dbItemDao.clearForDb(dbId)
            if (remoteItems.isNotEmpty()) {
                dbItemDao.insertItems(remoteItems.map { it.toEntity() })
            }
        }
    }

    override fun getApuResourcesForDbItem(itemId: String): Flow<List<ApuResourceItem>> {
        return dbItemResourceDao.getResourcesForDbItem(itemId).map { list ->
            list.map { it.toApuResourceItem() }
        }
    }

    override suspend fun syncDbItemResources(itemId: String) {
        runCatching {
            val remoteResources = remoteDataSource.getDbItemResources(itemId)
            dbItemResourceDao.clearForDbItem(itemId)
            if (remoteResources.isNotEmpty()) {
                dbItemResourceDao.insertDbItemResources(remoteResources.map { it.toEntity() })
            }
        }
    }

    override suspend fun addResourceToDbItem(itemId: String, resourceId: String, quantity: Double) {
        val itemResource = DbItemResource(
            itemId = itemId,
            resourceId = resourceId,
            quantity = quantity
        )
        runCatching {
            remoteDataSource.insertDbItemResource(itemResource)
        }
        dbItemResourceDao.insertDbItemResource(itemResource.toEntity())
    }

    override suspend fun deleteResourceFromDbItem(itemId: String, resourceId: String) {
        runCatching {
            remoteDataSource.deleteDbItemResource(itemId, resourceId)
        }
        dbItemResourceDao.deleteDbItemResource(itemId, resourceId)
    }

    override suspend fun addDbItem(dbId: String, name: String, code: String?, unit: String) {
        val uniqueSuffix = (1000..9999).random()
        val itemId = "${dbId}_item_${uniqueSuffix}"
        val item = DbSubItem(
            itemId = itemId,
            dbId = dbId,
            name = name,
            code = if (code.isNull_orBlank()) "ITEM-${uniqueSuffix}" else code,
            unit = if (unit.isNull_orBlank()) "unid" else unit
        )
        runCatching {
            remoteDataSource.insertDbItem(item)
        }
        dbItemDao.insertItems(listOf(item.toEntity()))
    }
}

private fun String?.isNull_orBlank(): Boolean = this == null || this.isBlank()
