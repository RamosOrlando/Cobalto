package com.cobalto.features.section.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.section.domain.model.Section
import com.cobalto.features.section.domain.use_case.GetSectionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SectionUiState {
    object Loading : SectionUiState
    data class Success(val sections: List<Section>) : SectionUiState
    data class Error(val message: String) : SectionUiState
}

sealed interface SectionEvent {
    data class OnSearchQueryChanged(val query: String) : SectionEvent
    data class SetCategory(val categoryId: String?) : SectionEvent
    object Refresh : SectionEvent
}

class SectionViewModel(
    private val getSectionsUseCase: GetSectionsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryId = MutableStateFlow<String?>(null)

    private val _refreshError = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SectionUiState> = combine(_searchQuery, _categoryId) { query, categoryId ->
        query to categoryId
    }.flatMapLatest { (query, categoryId) ->
        getSectionsUseCase.executeFlow(query, categoryId)
    }.map { resource ->
        when (resource) {
            is ResultState.Loading -> SectionUiState.Loading
            is ResultState.Error -> SectionUiState.Error(resource.message)
            is ResultState.Success<List<Section>> -> SectionUiState.Success(resource.data)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SectionUiState.Loading
    )

    fun onEvent(event: SectionEvent) {
        when (event) {
            is SectionEvent.OnSearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is SectionEvent.SetCategory -> {
                _categoryId.value = event.categoryId
            }
            SectionEvent.Refresh -> {
                viewModelScope.launch {
                    _refreshError.value = null
                    val result = getSectionsUseCase.refresh()
                    if (result is ResultState.Error) {
                        _refreshError.value = result.message
                    }
                }
            }
        }
    }
}
