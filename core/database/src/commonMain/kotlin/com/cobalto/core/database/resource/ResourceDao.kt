package com.cobalto.core.database.resource

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Query("SELECT * FROM Resource ORDER BY resourceId ASC")
    fun getResources(): Flow<List<ResourceEntity>>

    @Query("""
        SELECT DISTINCT m.* FROM Resource m
        LEFT JOIN Maker mk ON m.makerId = mk.makerId
        LEFT JOIN PriceHistory ph ON m.resourceId = ph.resourceId
        LEFT JOIN Provider p ON ph.providerId = p.providerId
        WHERE (:sectionId IS NULL OR m.sectionId = :sectionId)
        AND (:query = '' 
             OR m.name LIKE '%' || :query || '%' 
             OR mk.name LIKE '%' || :query || '%'
             OR p.name LIKE '%' || :query || '%')
        ORDER BY m.resourceId ASC
    """)
    fun getResourcesFiltered(query: String, sectionId: String?): Flow<List<ResourceEntity>>

    @Upsert
    suspend fun insertResources(resources: List<ResourceEntity>)

    @Upsert
    suspend fun insertResource(resource: ResourceEntity)

    @Query("SELECT COALESCE(MAX(CAST(SUBSTR(resourceId, LENGTH(:sectionId) + 2, 3) AS INTEGER)), 0) FROM Resource WHERE sectionId = :sectionId")
    suspend fun getResourceCount(sectionId: String): Int

    @Query("SELECT COALESCE(MAX(CAST(SUBSTR(resourceId, LENGTH(:sectionId) + 2, 3) AS INTEGER)), 0) FROM Resource WHERE sectionId = :sectionId")
    fun getResourceCountFlow(sectionId: String): Flow<Int>

    @Query("DELETE FROM Resource WHERE resourceId = :resourceId")
    suspend fun deleteResource(resourceId: String)

    @Query("DELETE FROM Resource")
    suspend fun clearAll()

    @Query("""
        UPDATE Resource
        SET 
            price = (
                SELECT price FROM PriceHistory 
                WHERE resourceId = :resourceId 
                ORDER BY SUBSTR(quoteDate,7,4) || SUBSTR(quoteDate,4,2) || SUBSTR(quoteDate,1,2) DESC 
                LIMIT 1
            ),
            quoteDate = (
                SELECT quoteDate FROM PriceHistory 
                WHERE resourceId = :resourceId 
                ORDER BY SUBSTR(quoteDate,7,4) || SUBSTR(quoteDate,4,2) || SUBSTR(quoteDate,1,2) DESC 
                LIMIT 1
            ),
            providerId = (
                SELECT providerId FROM PriceHistory 
                WHERE resourceId = :resourceId 
                ORDER BY SUBSTR(quoteDate,7,4) || SUBSTR(quoteDate,4,2) || SUBSTR(quoteDate,1,2) DESC 
                LIMIT 1
            )
        WHERE resourceId = :resourceId
    """)
    suspend fun updateResourceCurrentPriceFromHistory(resourceId: String)

    @Query("""
        UPDATE Resource
        SET 
            price = (
                SELECT price FROM PriceHistory 
                WHERE resourceId = Resource.resourceId 
                ORDER BY SUBSTR(quoteDate,7,4) || SUBSTR(quoteDate,4,2) || SUBSTR(quoteDate,1,2) DESC 
                LIMIT 1
            ),
            quoteDate = (
                SELECT quoteDate FROM PriceHistory 
                WHERE resourceId = Resource.resourceId 
                ORDER BY SUBSTR(quoteDate,7,4) || SUBSTR(quoteDate,4,2) || SUBSTR(quoteDate,1,2) DESC 
                LIMIT 1
            ),
            providerId = (
                SELECT providerId FROM PriceHistory 
                WHERE resourceId = Resource.resourceId 
                ORDER BY SUBSTR(quoteDate,7,4) || SUBSTR(quoteDate,4,2) || SUBSTR(quoteDate,1,2) DESC 
                LIMIT 1
            )
    """)
    suspend fun syncAllResourcesCurrentPricesFromHistory()
}
