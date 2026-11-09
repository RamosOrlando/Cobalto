package com.cobalto.features.presupuesto.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.features.presupuesto.domain.model.Project
import com.cobalto.features.presupuesto.domain.model.ProjectModule
import com.cobalto.features.presupuesto.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val projects: StateFlow<List<Project>> = projectRepository.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    fun onShowCreateDialog() {
        _showCreateDialog.value = true
    }

    fun onDismissCreateDialog() {
        _showCreateDialog.value = false
    }

    fun createProject(name: String, description: String?) {
        viewModelScope.launch {
            projectRepository.createProject(name, description)
            _showCreateDialog.value = false
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
        }
    }

    fun getModulesForProject(projectId: String): Flow<List<ProjectModule>> {
        return projectRepository.getModulesForProject(projectId)
    }

    fun getProjectTotalAmount(projectId: String): Flow<Double> {
        return projectRepository.getProjectTotalAmount(projectId)
    }

    fun addModule(projectId: String, code: String, title: String) {
        viewModelScope.launch {
            projectRepository.addModule(projectId, code, title)
        }
    }

    fun deleteModule(moduleId: String) {
        viewModelScope.launch {
            projectRepository.deleteModule(moduleId)
        }
    }

    fun addItem(
        projectId: String,
        moduleId: String,
        code: String?,
        name: String,
        unit: String,
        quantity: Double,
        unitPrice: Double,
        dbId: String? = null,
        imagePath: String? = null
    ) {
        viewModelScope.launch {
            projectRepository.addItem(
                projectId = projectId,
                moduleId = moduleId,
                code = code,
                name = name,
                unit = unit,
                quantity = quantity,
                unitPrice = unitPrice,
                dbId = dbId,
                imagePath = imagePath
            )
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            projectRepository.deleteItem(itemId)
        }
    }
}
