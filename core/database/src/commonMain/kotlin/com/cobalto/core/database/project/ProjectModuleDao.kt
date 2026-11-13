package com.cobalto.core.database.project

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectModuleDao {
    @Query("SELECT * FROM ProjectModule WHERE projectId = :projectId ORDER BY orderIndex ASC, code ASC")
    fun getModulesForProject(projectId: String): Flow<List<ProjectModuleEntity>>

    @Query("SELECT * FROM ProjectModule ORDER BY orderIndex ASC, code ASC")
    fun getAllModules(): Flow<List<ProjectModuleEntity>>

    @Upsert
    suspend fun insertModule(module: ProjectModuleEntity)

    @Query("DELETE FROM ProjectModule WHERE moduleId = :moduleId")
    suspend fun deleteModule(moduleId: String)

    @Query("DELETE FROM ProjectModule WHERE projectId = :projectId")
    suspend fun deleteModulesForProject(projectId: String)

    @Query("DELETE FROM ProjectModule")
    suspend fun clearAllModules()
}
