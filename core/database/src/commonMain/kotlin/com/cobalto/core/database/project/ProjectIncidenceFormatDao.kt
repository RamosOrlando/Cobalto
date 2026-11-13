package com.cobalto.core.database.project

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectIncidenceFormatDao {
    @Query("SELECT * FROM ProjectIncidenceFormat WHERE projectId = :projectId ORDER BY incidenceId ASC")
    fun getIncidenceFormatsForProject(projectId: String): Flow<List<ProjectIncidenceFormatEntity>>

    @Query("SELECT * FROM ProjectIncidenceFormat ORDER BY incidenceId ASC")
    fun getAllIncidenceFormats(): Flow<List<ProjectIncidenceFormatEntity>>

    @Upsert
    suspend fun insertIncidenceFormats(formats: List<ProjectIncidenceFormatEntity>)

    @Query("DELETE FROM ProjectIncidenceFormat WHERE projectId = :projectId")
    suspend fun deleteIncidenceFormatsForProject(projectId: String)

    @Query("DELETE FROM ProjectIncidenceFormat")
    suspend fun clearAll()
}
