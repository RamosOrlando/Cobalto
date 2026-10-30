package com.cobalto.features.resource.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.common.domain.model.Resource
import com.cobalto.features.resource.domain.use_case.GetResourcesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

sealed interface ResourcesSelectedUiState {
    object Loading : ResourcesSelectedUiState
    data class Success(val resources: List<Resource>) : ResourcesSelectedUiState
    data class Error(val message: String) : ResourcesSelectedUiState
}

class ResourcesSelectedViewModel(
    private val resourceIds: List<String>,
    private val initialQuantities: Map<String, Double> = emptyMap(),
    private val getResourcesUseCase: GetResourcesUseCase
) : ViewModel() {

    private val _quantities = MutableStateFlow<Map<String, Double>>(
        if (initialQuantities.isEmpty()) resourceIds.associateWith { 1.0 } else initialQuantities
    )
    val quantities = _quantities.asStateFlow()

    fun updateQuantity(id: String, quantity: Double) {
        _quantities.update { current ->
            current + (id to quantity)
        }
    }

    val uiState: StateFlow<ResourcesSelectedUiState> = getResourcesUseCase.executeFlow()
        .map { resource ->
            when (resource) {
                is ResultState.Loading -> ResourcesSelectedUiState.Loading
                is ResultState.Error -> ResourcesSelectedUiState.Error(resource.message)
                is ResultState.Success<List<Resource>> -> {
                    val filtered = resource.data.filter { it.resourceId in resourceIds }
                    ResourcesSelectedUiState.Success(filtered)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ResourcesSelectedUiState.Loading
        )
}
