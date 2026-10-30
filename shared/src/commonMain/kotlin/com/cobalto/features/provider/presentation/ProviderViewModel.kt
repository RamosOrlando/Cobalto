package com.cobalto.features.provider.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.provider.domain.model.Provider
import com.cobalto.features.provider.domain.use_case.GetProvidersUseCase
import com.cobalto.features.provider.domain.use_case.SaveProviderUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class CreateProviderUiState(
    val showAddDialog: Boolean = false,
    val editingProvider: Provider? = null,
    val name: String = "",
    val address: String = "",
    val telephone: String = "",
    val city: String = "Oruro",
    val email: String = "",
    val imagePath: String = "",
    val error: String? = null,
    val isLoading: Boolean = false
)

sealed interface ProviderUiState {
    object Loading : ProviderUiState
    data class Success(val providers: List<Provider>) : ProviderUiState
    data class Error(val message: String) : ProviderUiState
}

sealed interface ProviderEvent {
    data class OnSearchQueryChanged(val query: String) : ProviderEvent
    object Refresh : ProviderEvent
    object OnShowAddDialog : ProviderEvent
    data class OnShowEditDialog(val provider: Provider) : ProviderEvent
    object OnDismissAddDialog : ProviderEvent
    data class OnNameChanged(val name: String) : ProviderEvent
    data class OnAddressChanged(val address: String) : ProviderEvent
    data class OnTelephoneChanged(val telephone: String) : ProviderEvent
    data class OnCityChanged(val city: String) : ProviderEvent
    data class OnEmailChanged(val email: String) : ProviderEvent
    data class OnImagePathChanged(val path: String) : ProviderEvent
    object OnSaveProvider : ProviderEvent
}

class ProviderViewModel(
    private val getProvidersUseCase: GetProvidersUseCase,
    private val saveProviderUseCase: SaveProviderUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _createProviderState = MutableStateFlow(CreateProviderUiState())
    val createProviderState = _createProviderState.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<ProviderUiState> = combine(
        _searchQuery.debounce(300.milliseconds).flatMapLatest { query ->
            getProvidersUseCase.executeFlow(query)
        },
        _refreshError
    ) { resource, refreshError ->
        when {
            refreshError != null -> ProviderUiState.Error(refreshError)
            resource is ResultState.Loading -> ProviderUiState.Loading
            resource is ResultState.Error -> ProviderUiState.Error(resource.message)
            resource is ResultState.Success -> ProviderUiState.Success(resource.data)
            else -> ProviderUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProviderUiState.Loading
    )

    init {
        onEvent(ProviderEvent.Refresh)
    }

    fun onEvent(event: ProviderEvent) {
        when (event) {
            is ProviderEvent.OnSearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            ProviderEvent.Refresh -> {
                viewModelScope.launch {
                    _refreshError.value = null
                    val result = getProvidersUseCase.refresh()
                    if (result is ResultState.Error) {
                        _refreshError.value = result.message
                    }
                }
            }
            ProviderEvent.OnShowAddDialog -> {
                _createProviderState.update {
                    CreateProviderUiState(
                        showAddDialog = true,
                        editingProvider = null,
                        name = "",
                        address = "",
                        telephone = "",
                        city = "Oruro",
                        email = "",
                        imagePath = "",
                        error = null
                    )
                }
            }
            is ProviderEvent.OnShowEditDialog -> {
                val provider = event.provider
                _createProviderState.update {
                    CreateProviderUiState(
                        showAddDialog = true,
                        editingProvider = provider,
                        name = provider.name,
                        address = provider.address ?: "",
                        telephone = provider.telephone ?: "",
                        city = if (provider.city.isBlank()) "Oruro" else provider.city,
                        email = provider.email ?: "",
                        imagePath = provider.imagePath ?: "",
                        error = null
                    )
                }
            }
            ProviderEvent.OnDismissAddDialog -> {
                _createProviderState.update { CreateProviderUiState() }
            }
            is ProviderEvent.OnNameChanged -> {
                _createProviderState.update { it.copy(name = event.name, error = null) }
            }
            is ProviderEvent.OnAddressChanged -> {
                _createProviderState.update { it.copy(address = event.address, error = null) }
            }
            is ProviderEvent.OnTelephoneChanged -> {
                _createProviderState.update { it.copy(telephone = event.telephone, error = null) }
            }
            is ProviderEvent.OnCityChanged -> {
                _createProviderState.update { it.copy(city = event.city, error = null) }
            }
            is ProviderEvent.OnEmailChanged -> {
                _createProviderState.update { it.copy(email = event.email, error = null) }
            }
            is ProviderEvent.OnImagePathChanged -> {
                _createProviderState.update { it.copy(imagePath = event.path, error = null) }
            }
            ProviderEvent.OnSaveProvider -> {
                saveProvider()
            }
        }
    }

    private fun saveProvider() {
        val state = createProviderState.value
        val name = state.name.trim()
        val address = state.address.trim()
        val telephone = state.telephone.trim()
        val city = state.city.trim()
        val email = state.email.trim()
        val imagePath = state.imagePath.trim()

        if (name.isEmpty()) {
            _createProviderState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        val editingProvider = state.editingProvider
        val currentProviders = (uiState.value as? ProviderUiState.Success)?.providers ?: emptyList()

        if (currentProviders.any {
                it.providerId != editingProvider?.providerId &&
                it.name.trim().equals(name, ignoreCase = true) &&
                (it.address?.trim() ?: "").equals(address, ignoreCase = true) &&
                it.city.trim().equals(city, ignoreCase = true)
            }) {
            _createProviderState.update { it.copy(error = "El proveedor ya existe con este nombre, dirección y ciudad") }
            return
        }

        viewModelScope.launch {
            _createProviderState.update { it.copy(isLoading = true) }

            val providerToSave = if (editingProvider != null) {
                editingProvider.copy(
                    name = name,
                    address = if (address.isEmpty()) null else address,
                    telephone = if (telephone.isEmpty()) null else telephone,
                    city = city,
                    email = if (email.isEmpty()) null else email,
                    imagePath = if (imagePath.isEmpty()) null else imagePath
                )
            } else {
                val nextId = (currentProviders.mapNotNull { it.providerId.toIntOrNull() }.maxOrNull() ?: 0) + 1
                Provider(
                    providerId = nextId.toString(),
                    name = name,
                    address = if (address.isEmpty()) null else address,
                    telephone = if (telephone.isEmpty()) null else telephone,
                    city = city,
                    email = if (email.isEmpty()) null else email,
                    imagePath = if (imagePath.isEmpty()) null else imagePath
                )
            }

            val result = saveProviderUseCase.execute(providerToSave)
            when (result) {
                is ResultState.Success -> {
                    _createProviderState.update { CreateProviderUiState() }
                }
                is ResultState.Error -> {
                    _createProviderState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }
}
