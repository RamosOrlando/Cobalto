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
    val quantity: Double,
    val sectionId: String? = null
)

data class DbItemResourceCounts(
    val itemId: String,
    val materialsCount: Int = 0,
    val laborCount: Int = 0,
    val equipmentCount: Int = 0
)

@Dao
interface DbItemResourceDao {
    @Query("""
        SELECT dir.itemId, dir.resourceId, COALESCE(r.name, dir.resourceId) AS name, COALESCE(r.unit, 'unid') AS unit, r.price, dir.quantity, r.sectionId
        FROM DbItemResource dir
        LEFT JOIN Resource r ON dir.resourceId = r.resourceId
        WHERE dir.itemId = :itemId
    """)
    fun getResourcesForDbItem(itemId: String): Flow<List<DbItemResourceWithDetails>>

    @Query("""
        SELECT 
            dir.itemId AS itemId,
            COALESCE(SUM(CASE WHEN r.sectionId IS NULL OR (r.sectionId != 'Labor-Labor' AND r.sectionId != 'Equipment-Equipment') THEN 1 ELSE 0 END), 0) AS materialsCount,
            COALESCE(SUM(CASE WHEN r.sectionId = 'Labor-Labor' THEN 1 ELSE 0 END), 0) AS laborCount,
            COALESCE(SUM(CASE WHEN r.sectionId = 'Equipment-Equipment' THEN 1 ELSE 0 END), 0) AS equipmentCount
        FROM DbItemResource dir
        LEFT JOIN Resource r ON dir.resourceId = r.resourceId
        GROUP BY dir.itemId
    """)
    fun getAllResourceCountsGroupedByItem(): Flow<List<DbItemResourceCounts>>

    @Upsert
    suspend fun insertDbItemResources(itemResources: List<DbItemResourceEntity>)

    @Upsert
    suspend fun insertDbItemResource(itemResource: DbItemResourceEntity)

    @Query("DELETE FROM DbItemResource WHERE itemId = :itemId AND resourceId = :resourceId")
    suspend fun deleteDbItemResource(itemId: String, resourceId: String)

    @Query("DELETE FROM DbItemResource WHERE itemId = :itemId")
    suspend fun clearForDbItem(itemId: String)
}
