package com.cobalto.core.database.project

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM Project ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM Project WHERE projectId = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Upsert
    suspend fun insertProject(project: ProjectEntity)

    @Query("DELETE FROM Project WHERE projectId = :projectId")
    suspend fun deleteProject(projectId: String)
}
