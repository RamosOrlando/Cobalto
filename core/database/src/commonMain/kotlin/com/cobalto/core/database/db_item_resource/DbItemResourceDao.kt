package com.cobalto.core.database.db_item_resource

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class DbItemResourceWithDetails(
    val itemId: String,
    val resourceId: String,
    val name: String,
    val unit: String,
    val price: Double?,
    val quantity: Double
)

@Dao
interface DbItemResourceDao {
    @Query("""
        SELECT dir.itemId, dir.resourceId, COALESCE(r.name, dir.resourceId) AS name, COALESCE(r.unit, 'unid') AS unit, r.price, dir.quantity
        FROM DbItemResource dir
        LEFT JOIN Resource r ON dir.resourceId = r.resourceId
        WHERE dir.itemId = :itemId
    """)
    fun getResourcesForDbItem(itemId: String): Flow<List<DbItemResourceWithDetails>>

    @Upsert
    suspend fun insertDbItemResources(itemResources: List<DbItemResourceEntity>)

    @Upsert
    suspend fun insertDbItemResource(itemResource: DbItemResourceEntity)

    @Query("DELETE FROM DbItemResource WHERE itemId = :itemId AND resourceId = :resourceId")
    suspend fun deleteDbItemResource(itemId: String, resourceId: String)

    @Query("DELETE FROM DbItemResource WHERE itemId = :itemId")
    suspend fun clearForDbItem(itemId: String)
}
