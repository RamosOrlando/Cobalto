package com.cobalto.features.price_history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.resource.domain.model.ResourceWithPrices
import com.cobalto.features.resource.domain.repository.ResourceRepository
import com.cobalto.features.price_history.domain.use_case.GetPriceHistoryUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

sealed interface PriceHistoryUiState {
    object Loading : PriceHistoryUiState
    data class Success(val history: List<ResourceWithPrices>) : PriceHistoryUiState
    data class Error(val message: String) : PriceHistoryUiState
}

sealed interface PriceHistoryEvent {
    data class OnSearchQueryChanged(val query: String) : PriceHistoryEvent
    object Refresh : PriceHistoryEvent
}

class PriceHistoryViewModel(
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val repository: ResourceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val uiState: StateFlow<PriceHistoryUiState> = _searchQuery
        .debounce(300.milliseconds)
        .flatMapLatest { query ->
            getPriceHistoryUseCase.executeFlow(query)
        }
        .map { resource ->
            when (resource) {
                is ResultState.Loading -> PriceHistoryUiState.Loading
                is ResultState.Error -> PriceHistoryUiState.Error(resource.message)
                is ResultState.Success<List<ResourceWithPrices>> -> PriceHistoryUiState.Success(resource.data)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PriceHistoryUiState.Loading
        )

    init {
        onEvent(PriceHistoryEvent.Refresh)
    }

    fun onEvent(event: PriceHistoryEvent) {
        when (event) {
            is PriceHistoryEvent.OnSearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            PriceHistoryEvent.Refresh -> {
                viewModelScope.launch {
                    _isRefreshing.value = true
                    _refreshError.value = null
                    val result = repository.refreshResources()
                    if (result is ResultState.Error) {
                        _refreshError.value = result.message
                    }
                    _isRefreshing.value = false
                }
            }
        }
    }
}
