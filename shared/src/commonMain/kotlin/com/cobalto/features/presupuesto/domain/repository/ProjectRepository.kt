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
    suspend fun deleteModule(moduleId: String)

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
    suspend fun updateItemQuantityAndPrice(itemId: String, quantity: Double, unitPrice: Double)
    suspend fun deleteItem(itemId: String)
    fun getProjectTotalAmount(projectId: String): Flow<Double>
}
