package com.cobalto.features.presupuesto.domain.repository

import com.cobalto.features.presupuesto.domain.model.Project
import com.cobalto.features.presupuesto.domain.model.ProjectItem
import com.cobalto.features.presupuesto.domain.model.ProjectModule
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAllProjects(): Flow<List<Project>>
    suspend fun createProject(name: String, description: String?): Result<Project>
    suspend fun deleteProject(projectId: String)

    fun getModulesForProject(projectId: String): Flow<List<ProjectModule>>
    suspend fun addModule(projectId: String, code: String, title: String)
    suspend fun deleteModule(projectId: String, moduleId: String)

    suspend fun addItem(
        projectId: String,
        moduleId: String,
        code: String?,
        name: String,
        unit: String,
        quantity: Double,
        unitPrice: Double,
        dbId: String? = null,
        imagePath: String? = null
    )
    suspend fun updateItemQuantityAndPrice(projectId: String, itemId: String, quantity: Double, unitPrice: Double)
    suspend fun deleteItem(projectId: String, itemId: String)
    fun getProjectTotalAmount(projectId: String): Flow<Double>
    suspend fun importDbItemToModule(projectId: String, moduleId: String, dbSubItem: com.cobalto.features.db.domain.model.DbSubItem)
    fun getProjectIncidenceFormats(projectId: String): Flow<List<com.cobalto.features.incidence_format.domain.model.IncidenceFormat>>
    suspend fun saveProjectIncidenceFormats(projectId: String, formats: List<com.cobalto.features.incidence_format.domain.model.IncidenceFormat>)
}
