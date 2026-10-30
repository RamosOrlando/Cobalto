package com.cobalto.core.database.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DbDao {
    @Query("SELECT * FROM Db ORDER BY name ASC")
    fun getAllDbs(): Flow<List<DbEntity>>

    @Upsert
    suspend fun insertDbs(dbs: List<DbEntity>)

    @Upsert
    suspend fun insertDb(db: DbEntity)

    @Query("DELETE FROM Db WHERE dbId = :dbId")
    suspend fun deleteDb(dbId: String)

    @Query("DELETE FROM Db")
    suspend fun clearAll()
}
