package com.cobalto.features.presupuesto.data.repository

import com.cobalto.core.common.util.getCurrentIsoDate
import com.cobalto.core.common.util.randomUUID
import com.cobalto.core.database.AppDatabase
import com.cobalto.core.database.ProjectDatabaseManager
import com.cobalto.core.database.db_item_resource.DbItemResourceDao
import com.cobalto.core.database.project.ProjectDao
import com.cobalto.core.database.project.ProjectEntity
import com.cobalto.core.database.project.ProjectIncidenceFormatEntity
import com.cobalto.core.database.project.ProjectItemEntity
import com.cobalto.core.database.project.ProjectItemResourceEntity
import com.cobalto.core.database.project.ProjectModuleEntity
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.incidence_format.domain.model.IncidenceFormat
import com.cobalto.features.presupuesto.domain.model.Project
import com.cobalto.features.presupuesto.domain.model.ProjectItem
import com.cobalto.features.presupuesto.domain.model.ProjectModule
import com.cobalto.features.presupuesto.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ProjectRepositoryImpl(
    private val projectDao: ProjectDao,
    private val dbItemResourceDao: DbItemResourceDao,
    private val projectDatabaseManager: ProjectDatabaseManager
) : ProjectRepository {

    private suspend fun getProjectDatabase(projectId: String): AppDatabase {
        val project = projectDao.getProjectById(projectId)
        val filePath = project?.filePath ?: "projects/$projectId.db"
        return projectDatabaseManager.getDatabaseForFile(filePath)
    }

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

            // Connect to per-project autonomous SQLite .db file
            val projectDb = projectDatabaseManager.getDatabaseForFile(filePath)

            // Insert default module M01 PRESUPUESTO in this project's .db file
            val defaultModule = ProjectModuleEntity(
                moduleId = randomUUID(),
                projectId = projectId,
                code = "M01",
                title = "PRESUPUESTO",
                orderIndex = 0
            )
            projectDb.projectModuleDao().insertModule(defaultModule)

            // Insert default project incidence formats in this project's .db file
            val defaultIncidences = listOf(
                ProjectIncidenceFormatEntity("A", projectId, "SUBT-MAT", "SUBTOTAL MATERIALES", 100.0, "1", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("B", projectId, "SUBT-MO", "SUBTOTAL MANO DE OBRA", 100.0, "1", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("C", projectId, "SUBT-HE", "SUBTOTAL HERRAMIENTAS Y EQUIPO", 100.0, "1", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("D", projectId, "T-MAT", "TOTAL MATERIALES", 100.0, "A", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("E", projectId, "CS", "CARGAS SOCIALES", 55.0, "B", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("F", projectId, "IVA", "IMPUESTOS IVA MANO DE OBRA", 14.94, "B+E", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("G", projectId, "T-MO", "TOTAL MANO DE OBRA", 100.0, "B+E+F", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("H", projectId, "HM", "HERRAMIENTAS MENORES", 5.0, "C", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("I", projectId, "T-HE", "TOTAL HERRAMIENTAS Y EQUIPOS", 100.0, "C+H", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("J", projectId, "GGA", "GASTOS GENERALES Y ADMINISTRATIVOS", 10.0, "D+G+I", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("K", projectId, "UTIL", "UTILIDAD", 10.0, "D+G+I+J", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("L", projectId, "IT", "IMPUESTO A LAS TRANSACCIONES", 3.09, "D+G+I+J+K", "PU-PARCIAL", true),
                ProjectIncidenceFormatEntity("M", projectId, "T-PU", "TOTAL PRECIO UNITARIO", 100.0, "D+G+I+J+K+L", "PU-PARCIAL", true)
            )
            projectDb.projectIncidenceFormatDao().insertIncidenceFormats(defaultIncidences)

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
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDatabaseManager.deleteProjectDatabaseFile(project.filePath)
        }
        projectDao.deleteProject(projectId)
    }

    override fun getModulesForProject(projectId: String): Flow<List<ProjectModule>> {
        return flow {
            val projectDb = getProjectDatabase(projectId)
            emitAll(
                combine(
                    projectDb.projectModuleDao().getModulesForProject(projectId),
                    projectDb.projectItemDao().getAllItems()
                ) { modules, items ->
                    val firstModuleId = modules.firstOrNull()?.moduleId
                    val itemsByModule = items.groupBy { item ->
                        if (modules.any { it.moduleId == item.moduleId }) item.moduleId else (firstModuleId ?: item.moduleId)
                    }
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
            )
        }
    }

    override suspend fun addModule(projectId: String, code: String, title: String) {
        val projectDb = getProjectDatabase(projectId)
        val entity = ProjectModuleEntity(
            moduleId = randomUUID(),
            projectId = projectId,
            code = code.trim(),
            title = title.trim()
        )
        projectDb.projectModuleDao().insertModule(entity)
    }

    override suspend fun deleteModule(projectId: String, moduleId: String) {
        val projectDb = getProjectDatabase(projectId)
        projectDb.projectItemDao().deleteItemsForModule(moduleId)
        projectDb.projectModuleDao().deleteModule(moduleId)
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
        val projectDb = getProjectDatabase(projectId)
        val modules = projectDb.projectModuleDao().getModulesForProject(projectId).first()
        val validModuleId = if (modules.any { it.moduleId == moduleId }) {
            moduleId
        } else {
            modules.firstOrNull()?.moduleId ?: moduleId
        }
        val entity = ProjectItemEntity(
            itemId = randomUUID(),
            moduleId = validModuleId,
            name = name.trim(),
            unit = unit.trim(),
            dbId = dbId,
            code = code?.trim(),
            imagePath = imagePath,
            unitPrice = unitPrice,
            quantity = quantity,
            totalAmount = quantity * unitPrice
        )
        projectDb.projectItemDao().insertItem(entity)
    }

    override suspend fun updateItemQuantityAndPrice(projectId: String, itemId: String, quantity: Double, unitPrice: Double) {
        val projectDb = getProjectDatabase(projectId)
        projectDb.projectItemDao().updateItemQuantityAndPrice(itemId, quantity, unitPrice)
    }

    override suspend fun deleteItem(projectId: String, itemId: String) {
        val projectDb = getProjectDatabase(projectId)
        projectDb.projectItemDao().deleteItem(itemId)
    }

    override fun getProjectTotalAmount(projectId: String): Flow<Double> {
        return flow {
            val projectDb = getProjectDatabase(projectId)
            emitAll(projectDb.projectItemDao().getTotalAmountForProject(projectId))
        }
    }

    override suspend fun importDbItemToModule(
        projectId: String,
        moduleId: String,
        dbSubItem: DbSubItem
    ) {
        val projectDb = getProjectDatabase(projectId)
        val modules = projectDb.projectModuleDao().getModulesForProject(projectId).first()
        val validModuleId = if (modules.any { it.moduleId == moduleId }) {
            moduleId
        } else {
            modules.firstOrNull()?.moduleId ?: moduleId
        }
        val newItemId = randomUUID()
        val entity = ProjectItemEntity(
            itemId = newItemId,
            moduleId = validModuleId,
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
        projectDb.projectItemDao().insertItem(entity)

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
                projectDb.projectItemResourceDao().insertProjectItemResources(projectResources)
            }
        }
    }

    override fun getProjectIncidenceFormats(projectId: String): Flow<List<IncidenceFormat>> {
        return flow {
            val projectDb = getProjectDatabase(projectId)
            emitAll(
                projectDb.projectIncidenceFormatDao().getIncidenceFormatsForProject(projectId).map { list ->
                    list.map { entity ->
                        IncidenceFormat(
                            incidenceId = entity.incidenceId,
                            codigo = entity.codigo,
                            name = entity.name,
                            percent = entity.percent,
                            formula = entity.formula,
                            decimalId = entity.decimalId,
                            isVisible = entity.isVisible
                        )
                    }
                }
            )
        }
    }

    override suspend fun saveProjectIncidenceFormats(projectId: String, formats: List<IncidenceFormat>) {
        val projectDb = getProjectDatabase(projectId)
        val entities = formats.map { format ->
            ProjectIncidenceFormatEntity(
                incidenceId = format.incidenceId,
                projectId = projectId,
                codigo = format.codigo,
                name = format.name,
                percent = format.percent,
                formula = format.formula,
                decimalId = format.decimalId,
                isVisible = format.isVisible
            )
        }
        projectDb.projectIncidenceFormatDao().insertIncidenceFormats(entities)
    }
}
