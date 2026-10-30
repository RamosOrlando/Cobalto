package com.cobalto.features.resource.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.common.domain.model.Resource
import com.cobalto.features.resource.domain.use_case.GetResourcesUseCase
import com.cobalto.features.maker.domain.use_case.GetMakersUseCase
import com.cobalto.features.price_history.domain.repository.PriceHistoryRepository
import com.cobalto.features.auth.domain.repository.AuthRepository
import com.cobalto.features.price_history.domain.model.PriceHistory
import com.cobalto.core.common.util.getCurrentDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class ResourceItem(
    val resource: Resource,
    val makerName: String,
    val providerName: String? = null
)

sealed interface ResourceUiState {
    object Loading : ResourceUiState
    data class Success(
        val resources: List<ResourceItem>,
        val providers: List<com.cobalto.features.provider.domain.model.Provider> = emptyList(),
        val makers: List<com.cobalto.features.maker.domain.model.Maker> = emptyList(),
        val errorMessage: String? = null
    ) : ResourceUiState
    data class Error(val message: String) : ResourceUiState
}

sealed interface ResourceEvent {
    data class OnSearchQueryChanged(val query: String) : ResourceEvent
    data class SetSection(val sectionId: String?) : ResourceEvent
    data class ToggleResourceSelection(val materialId: String) : ResourceEvent
    data class UpdateResource(val material: Resource) : ResourceEvent
    data class CreateResource(val material: Resource) : ResourceEvent
    data class DeleteResource(val materialId: String) : ResourceEvent
    data class BulkUpdateMaterials(val updatedMaterials: List<Resource>) : ResourceEvent
    object ClearError : ResourceEvent
    object ClearSelection : ResourceEvent
    object Refresh : ResourceEvent
}

class ResourceViewModel(
    private val getResourcesUseCase: GetResourcesUseCase,
    private val getMakersUseCase: GetMakersUseCase,
    private val getProvidersUseCase: com.cobalto.features.provider.domain.use_case.GetProvidersUseCase,
    private val priceHistoryRepository: PriceHistoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMaterialIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedMaterialIds: StateFlow<Set<String>> = _selectedMaterialIds.asStateFlow()

    private val _sectionId = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val nextIndex: StateFlow<Int> = _sectionId
        .flatMapLatest { id ->
            if (id != null) getResourcesUseCase.getNextIndexFlow(id)
            else emptyFlow()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)

    val userEmail: String? = authRepository.getCurrentUserEmail()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val uiState: StateFlow<ResourceUiState> = combine(
        combine(_searchQuery, _sectionId) { query, sectionId ->
            query to sectionId
        }.debounce(300.milliseconds)
            .flatMapLatest { (query, sectionId) ->
                getResourcesUseCase.executeFlow(query, sectionId)
            },
        getMakersUseCase.executeFlow(""),
        getProvidersUseCase.executeFlow(""),
        _refreshError
    ) { materialsResource, makersResource, providersResource, refreshError ->
        when {
            materialsResource is ResultState.Loading || makersResource is ResultState.Loading || providersResource is ResultState.Loading -> ResourceUiState.Loading
            materialsResource is ResultState.Error -> ResourceUiState.Error(materialsResource.message)
            makersResource is ResultState.Error -> ResourceUiState.Error(makersResource.message)
            providersResource is ResultState.Error -> ResourceUiState.Error(providersResource.message)
            materialsResource is ResultState.Success && makersResource is ResultState.Success && providersResource is ResultState.Success -> {
                val makersMap = makersResource.data.associateBy { it.makerId }
                val providersMap = providersResource.data.associateBy { it.providerId }
                val resourceItems = materialsResource.data.map { material ->
                    ResourceItem(
                        resource = material,
                        makerName = makersMap[material.makerId]?.name ?: "Desconocido",
                        providerName = providersMap[material.providerId]?.name
                    )
                }
                ResourceUiState.Success(
                    resources = resourceItems,
                    providers = providersResource.data,
                    makers = makersResource.data,
                    errorMessage = refreshError
                )
            }
            else -> ResourceUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResourceUiState.Loading
    )

    fun onEvent(event: ResourceEvent) {
        when (event) {
            is ResourceEvent.OnSearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is ResourceEvent.SetSection -> {
                _sectionId.value = event.sectionId
            }
            is ResourceEvent.ToggleResourceSelection -> {
                _selectedMaterialIds.value = _selectedMaterialIds.value.let { current ->
                    if (current.contains(event.materialId)) current - event.materialId
                    else current + event.materialId
                }
            }
            ResourceEvent.ClearSelection -> {
                _selectedMaterialIds.value = emptySet()
            }
            ResourceEvent.ClearError -> {
                _refreshError.value = null
            }
            is ResourceEvent.UpdateResource -> {
                viewModelScope.launch {
                    _isRefreshing.value = true
                    _refreshError.value = null
                    
                    try {
                        val matPrice = event.material.price
                        val matProviderId = event.material.providerId
                        // 1. Primero intentamos guardar en el historial si hay precio y proveedor
                        if (matPrice != null && matProviderId != null) {
                            val historyEntry = PriceHistory(
                                historyId = com.cobalto.core.common.util.randomUUID(),
                                materialId = event.material.resourceId,
                                providerId = matProviderId,
                                price = matPrice,
                                quoteDate = event.material.quoteDate ?: getCurrentDate(),
                                username = authRepository.getCurrentUserEmail() ?: "anon"
                            )
                            val historyResult = priceHistoryRepository.upsertPriceHistory(historyEntry)
                            if (historyResult is ResultState.Error) {
                                throw Exception(historyResult.message)
                            }
                        }

                        // 2. Si el historial se guardó bien, actualizamos el material
                        val materialResult = getResourcesUseCase.updateResource(event.material)
                        if (materialResult is ResultState.Error) {
                            throw Exception(materialResult.message)
                        }
                    } catch (e: Exception) {
                        _refreshError.value = e.message
                    } finally {
                        _isRefreshing.value = false
                    }
                }
            }
            is ResourceEvent.CreateResource -> {
                viewModelScope.launch {
                    _isRefreshing.value = true
                    _refreshError.value = null
                    val result = getResourcesUseCase.updateResource(event.material)
                    if (result is ResultState.Error) {
                        _refreshError.value = result.message
                    }
                    _isRefreshing.value = false
                }
            }
            is ResourceEvent.DeleteResource -> {
                viewModelScope.launch {
                    _isRefreshing.value = true
                    _refreshError.value = null
                    val result = getResourcesUseCase.deleteResource(event.materialId)
                    if (result is ResultState.Error) {
                        _refreshError.value = result.message
                    }
                    _isRefreshing.value = false
                }
            }
            is ResourceEvent.BulkUpdateMaterials -> {
                viewModelScope.launch {
                    _isRefreshing.value = true
                    _refreshError.value = null
                    
                    for (material in event.updatedMaterials) {
                        val result = getResourcesUseCase.updateResource(material)
                        if (result is ResultState.Error) {
                            _refreshError.value = result.message
                        } else {
                            val matPrice = material.price
                            val matProviderId = material.providerId
                            if (matPrice != null && matProviderId != null) {
                                val historyEntry = PriceHistory(
                                    historyId = com.cobalto.core.common.util.randomUUID(),
                                    materialId = material.resourceId,
                                    providerId = matProviderId,
                                    price = matPrice,
                                    quoteDate = material.quoteDate?.ifBlank { null } ?: getCurrentDate(),
                                    username = authRepository.getCurrentUserEmail() ?: "anon"
                                )
                                val historyResult = priceHistoryRepository.upsertPriceHistory(historyEntry)
                                if (historyResult is ResultState.Error) {
                                    _refreshError.value = historyResult.message
                                }
                            }
                        }
                    }
                    
                    _isRefreshing.value = false
                }
            }
            ResourceEvent.Refresh -> {
                viewModelScope.launch {
                    _isRefreshing.value = true
                    _refreshError.value = null
                    val result = getResourcesUseCase.refresh()
                    if (result is ResultState.Error) {
                        _refreshError.value = result.message
                    }
                    _isRefreshing.value = false
                }
            }
        }
    }
}
