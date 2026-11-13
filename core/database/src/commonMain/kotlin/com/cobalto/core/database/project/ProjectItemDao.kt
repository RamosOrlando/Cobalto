package com.cobalto.core.database.project

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectItemDao {
    @Query("SELECT * FROM ProjectItem ORDER BY code ASC")
    fun getAllItems(): Flow<List<ProjectItemEntity>>

    @Query("SELECT * FROM ProjectItem WHERE moduleId = :moduleId ORDER BY code ASC")
    fun getItemsForModule(moduleId: String): Flow<List<ProjectItemEntity>>

    @Upsert
    suspend fun insertItem(item: ProjectItemEntity)

    @Query("UPDATE ProjectItem SET quantity = :quantity, unitPrice = :unitPrice, totalAmount = :quantity * :unitPrice WHERE itemId = :itemId")
    suspend fun updateItemQuantityAndPrice(itemId: String, quantity: Double, unitPrice: Double)

    @Query("DELETE FROM ProjectItem WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM ProjectItem WHERE moduleId = :moduleId")
    suspend fun deleteItemsForModule(moduleId: String)

    @Query("DELETE FROM ProjectItem WHERE moduleId IN (SELECT moduleId FROM ProjectModule WHERE projectId = :projectId)")
    suspend fun deleteItemsForProject(projectId: String)

    @Query("DELETE FROM ProjectItem")
    suspend fun clearAllItems()

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM ProjectItem WHERE moduleId IN (SELECT moduleId FROM ProjectModule WHERE projectId = :projectId)")
    fun getTotalAmountForProject(projectId: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM ProjectItem")
    fun getTotalAmount(): Flow<Double>
}
