package com.cobalto.core.database.db_item

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class DbItemCountEntity(
    val dbId: String,
    val count: Int
)

@Dao
interface DbItemDao {
    @Query("SELECT dbId, COUNT(*) AS count FROM DbItem GROUP BY dbId")
    fun getItemCountsGroupedByDb(): Flow<List<DbItemCountEntity>>

    @Query("""
        SELECT 
            di.itemId,
            di.name,
            di.unit,
            di.dbId,
            di.Code,
            di.imagePath,
            COALESCE(
                di.total,
                (
                    SELECT SUM(dir.quantity * COALESCE(r.price, 0.0))
                    FROM DbItemResource dir
                    LEFT JOIN Resource r ON dir.resourceId = r.resourceId
                    WHERE dir.itemId = di.itemId
                )
            ) AS total
        FROM DbItem di
        ORDER BY di.name ASC
    """)
    fun getAllDbItems(): Flow<List<DbItemEntity>>
    @Query("""
        SELECT 
            di.itemId,
            di.name,
            di.unit,
            di.dbId,
            di.Code,
            di.imagePath,
            COALESCE(
                di.total,
                (
                    SELECT SUM(dir.quantity * COALESCE(r.price, 0.0))
                    FROM DbItemResource dir
                    LEFT JOIN Resource r ON dir.resourceId = r.resourceId
                    WHERE dir.itemId = di.itemId
                )
            ) AS total
        FROM DbItem di
        WHERE di.dbId = :dbId
        ORDER BY di.name ASC
    """)
    fun getItemsForDb(dbId: String): Flow<List<DbItemEntity>>

    @Query("""
        SELECT 
            di.itemId,
            di.name,
            di.unit,
            di.dbId,
            di.Code,
            di.imagePath,
            COALESCE(
                di.total,
                (
                    SELECT SUM(dir.quantity * COALESCE(r.price, 0.0))
                    FROM DbItemResource dir
                    LEFT JOIN Resource r ON dir.resourceId = r.resourceId
                    WHERE dir.itemId = di.itemId
                )
            ) AS total
        FROM DbItem di
        WHERE di.dbId = :dbId AND (di.name LIKE '%' || :query || '%' OR di.Code LIKE '%' || :query || '%')
        ORDER BY di.name ASC
    """)
    fun getItemsForDbFiltered(dbId: String, query: String): Flow<List<DbItemEntity>>

    @Upsert
    suspend fun insertItems(items: List<DbItemEntity>)

    @Query("UPDATE DbItem SET total = :total WHERE itemId = :itemId")
    suspend fun updateItemTotal(itemId: String, total: Double)

    @Query("DELETE FROM DbItem WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM DbItem WHERE dbId = :dbId")
    suspend fun clearForDb(dbId: String)
}
