package com.cobalto.core.database.provider

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Query("SELECT * FROM Provider ORDER BY providerId ASC")
    fun getProviders(): Flow<List<ProviderEntity>>

    @Upsert
    suspend fun insertProviders(providers: List<ProviderEntity>)

    @Query("DELETE FROM Provider")
    suspend fun clearAll()
}
