package com.cobalto.core.database.incidence_format

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidenceFormatDao {
    @Query("SELECT * FROM IncidenceFormat ORDER BY incidenceId ASC")
    fun getAllIncidenceFormats(): Flow<List<IncidenceFormatEntity>>

    @Query("SELECT * FROM IncidenceFormat WHERE codigo = :codigo LIMIT 1")
    suspend fun getIncidenceFormatByCodigo(codigo: String): IncidenceFormatEntity?

    @Query("SELECT * FROM IncidenceFormat WHERE incidenceId = :incidenceId LIMIT 1")
    suspend fun getIncidenceFormatById(incidenceId: String): IncidenceFormatEntity?

    @Upsert
    suspend fun insertIncidenceFormats(formats: List<IncidenceFormatEntity>)

    @Upsert
    suspend fun insertIncidenceFormat(format: IncidenceFormatEntity)

    @Query("DELETE FROM IncidenceFormat WHERE incidenceId = :incidenceId")
    suspend fun deleteIncidenceFormat(incidenceId: String)

    @Query("DELETE FROM IncidenceFormat")
    suspend fun clearAll()
}
