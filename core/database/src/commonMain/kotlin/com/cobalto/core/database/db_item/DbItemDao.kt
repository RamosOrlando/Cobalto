package com.cobalto.core.database.db_item

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DbItemDao {
    @Query("SELECT * FROM DbItem WHERE dbId = :dbId ORDER BY name ASC")
    fun getItemsForDb(dbId: String): Flow<List<DbItemEntity>>

    @Query("SELECT * FROM DbItem WHERE dbId = :dbId AND (name LIKE '%' || :query || '%' OR Code LIKE '%' || :query || '%') ORDER BY name ASC")
    fun getItemsForDbFiltered(dbId: String, query: String): Flow<List<DbItemEntity>>

    @Upsert
    suspend fun insertItems(items: List<DbItemEntity>)

    @Query("DELETE FROM DbItem WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM DbItem WHERE dbId = :dbId")
    suspend fun clearForDb(dbId: String)
}
