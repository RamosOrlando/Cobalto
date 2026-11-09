package com.cobalto.features.presupuesto.data.repository

import com.cobalto.core.common.util.getCurrentIsoDate
import com.cobalto.core.common.util.randomUUID
import com.cobalto.core.database.db_item_resource.DbItemResourceDao
import com.cobalto.core.database.project.ProjectDao
import com.cobalto.core.database.project.ProjectEntity
import com.cobalto.core.database.project.ProjectItemDao
import com.cobalto.core.database.project.ProjectItemEntity
import com.cobalto.core.database.project.ProjectItemResourceDao
import com.cobalto.core.database.project.ProjectItemResourceEntity
import com.cobalto.core.database.project.ProjectModuleDao
import com.cobalto.core.database.project.ProjectModuleEntity
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.presupuesto.domain.model.Project
import com.cobalto.features.presupuesto.domain.model.ProjectItem
import com.cobalto.features.presupuesto.domain.model.ProjectModule
import com.cobalto.features.presupuesto.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProjectRepositoryImpl(
    private val projectDao: ProjectDao,
    private val projectModuleDao: ProjectModuleDao,
    private val projectItemDao: ProjectItemDao,
    private val dbItemResourceDao: DbItemResourceDao,
    private val projectItemResourceDao: ProjectItemResourceDao
) : ProjectRepository {

    override fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { entity ->
                Project(
                    projectId = entity.projectId,
                    name = entity.name,
                    description = entity.description,
                    filePath = entity.filePath,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    totalAmount = entity.totalAmount
                )
            }
        }
    }

    override suspend fun createProject(name: String, description: String?): Result<Project> {
        return runCatching {
            val projectId = randomUUID()
            val now = getCurrentIsoDate()
            val safeFileName = name.lowercase().replace(Regex("[^a-z0-9_]"), "_")
            val fileName = "${safeFileName}_${projectId.take(8)}.db"
            val filePath = "projects/$fileName"

            val entity = ProjectEntity(
                projectId = projectId,
                name = name.trim(),
                description = description?.trim(),
                filePath = filePath,
                createdAt = now,
                updatedAt = now,
                totalAmount = 0.0
            )

            projectDao.insertProject(entity)

            // Insert default module M01 PRESUPUESTO
            val defaultModule = ProjectModuleEntity(
                moduleId = randomUUID(),
                code = "M01",
                title = "PRESUPUESTO",
                orderIndex = 0
            )
            projectModuleDao.insertModule(defaultModule)

            Project(
                projectId = entity.projectId,
                name = entity.name,
                description = entity.description,
                filePath = entity.filePath,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                totalAmount = entity.totalAmount
            )
        }
    }

    override suspend fun deleteProject(projectId: String) {
        projectModuleDao.clearAllModules()
        projectItemDao.clearAllItems()
        projectItemResourceDao.clearAllProjectItemResources()
        projectDao.deleteProject(projectId)
    }

    override fun getModulesForProject(projectId: String): Flow<List<ProjectModule>> {
        return combine(
            projectModuleDao.getAllModules(),
            projectItemDao.getAllItems()
        ) { modules, items ->
            val itemsByModule = items.groupBy { it.moduleId }
            modules.map { mod ->
                val moduleItems = (itemsByModule[mod.moduleId] ?: emptyList()).map { itemEntity ->
                    ProjectItem(
                        itemId = itemEntity.itemId,
                        moduleId = itemEntity.moduleId,
                        name = itemEntity.name,
                        unit = itemEntity.unit,
                        dbId = itemEntity.dbId,
                        code = itemEntity.code,
                        imagePath = itemEntity.imagePath,
                        unitPrice = itemEntity.unitPrice,
                        quantity = itemEntity.quantity,
                        totalAmount = itemEntity.totalAmount,
                        orderIndex = itemEntity.orderIndex
                    )
                }
                ProjectModule(
                    moduleId = mod.moduleId,
                    code = mod.code,
                    title = mod.title,
                    orderIndex = mod.orderIndex,
                    items = moduleItems
                )
            }
        }
    }

    override suspend fun addModule(projectId: String, code: String, title: String) {
        val entity = ProjectModuleEntity(
            moduleId = randomUUID(),
            code = code.trim(),
            title = title.trim()
        )
        projectModuleDao.insertModule(entity)
    }

    override suspend fun deleteModule(moduleId: String) {
        projectItemDao.deleteItemsForModule(moduleId)
        projectModuleDao.deleteModule(moduleId)
    }

    override suspend fun addItem(
        projectId: String,
        moduleId: String,
        code: String?,
        name: String,
        unit: String,
        quantity: Double,
        unitPrice: Double,
        dbId: String?,
        imagePath: String?
    ) {
        val entity = ProjectItemEntity(
            itemId = randomUUID(),
            moduleId = moduleId,
            name = name.trim(),
            unit = unit.trim(),
            dbId = dbId,
            code = code?.trim(),
            imagePath = imagePath,
            unitPrice = unitPrice,
            quantity = quantity,
            totalAmount = quantity * unitPrice
        )
        projectItemDao.insertItem(entity)
    }

    override suspend fun updateItemQuantityAndPrice(itemId: String, quantity: Double, unitPrice: Double) {
        projectItemDao.updateItemQuantityAndPrice(itemId, quantity, unitPrice)
    }

    override suspend fun deleteItem(itemId: String) {
        projectItemDao.deleteItem(itemId)
    }

    override fun getProjectTotalAmount(projectId: String): Flow<Double> {
        return projectItemDao.getTotalAmount()
    }

    override suspend fun importDbItemToModule(
        projectId: String,
        moduleId: String,
        dbSubItem: DbSubItem
    ) {
        val newItemId = randomUUID()
        val entity = ProjectItemEntity(
            itemId = newItemId,
            moduleId = moduleId,
            name = dbSubItem.name.trim(),
            unit = dbSubItem.unit.trim(),
            dbId = dbSubItem.dbId,
            code = dbSubItem.code?.trim(),
            imagePath = dbSubItem.imagePath,
            unitPrice = dbSubItem.total ?: 0.0,
            quantity = 1.0,
            totalAmount = dbSubItem.total ?: 0.0,
            orderIndex = 0
        )
        projectItemDao.insertItem(entity)

        runCatching {
            val resources = dbItemResourceDao.getResourcesForDbItem(dbSubItem.itemId).first()
            if (resources.isNotEmpty()) {
                val projectResources = resources.map { res ->
                    ProjectItemResourceEntity(
                        itemId = newItemId,
                        resourceId = res.resourceId,
                        quantity = res.quantity
                    )
                }
                projectItemResourceDao.insertProjectItemResources(projectResources)
            }
        }
    }
}
