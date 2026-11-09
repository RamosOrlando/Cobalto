package com.cobalto.core.database.project

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class ProjectItemResourceWithDetails(
    val itemId: String,
    val resourceId: String,
    val name: String,
    val unit: String,
    val price: Double?,
    val quantity: Double,
    val sectionId: String? = null
)

@Dao
interface ProjectItemResourceDao {
    @Query("""
        SELECT pir.itemId, pir.resourceId, COALESCE(r.name, pir.resourceId) AS name, COALESCE(r.unit, 'unid') AS unit, r.price, pir.quantity, r.sectionId
        FROM ProjectItemResource pir
        LEFT JOIN Resource r ON pir.resourceId = r.resourceId
        WHERE pir.itemId = :itemId
    """)
    fun getResourcesForProjectItem(itemId: String): Flow<List<ProjectItemResourceWithDetails>>

    @Upsert
    suspend fun insertProjectItemResources(itemResources: List<ProjectItemResourceEntity>)

    @Upsert
    suspend fun insertProjectItemResource(itemResource: ProjectItemResourceEntity)

    @Query("DELETE FROM ProjectItemResource WHERE itemId = :itemId AND resourceId = :resourceId")
    suspend fun deleteProjectItemResource(itemId: String, resourceId: String)

    @Query("DELETE FROM ProjectItemResource WHERE itemId = :itemId")
    suspend fun clearForProjectItem(itemId: String)

    @Query("DELETE FROM ProjectItemResource")
    suspend fun clearAllProjectItemResources()
}
