package com.cobalto.features.db.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.features.db.domain.model.DbItem
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.db.domain.repository.DbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DbUiState {
    data object Loading : DbUiState
    data class Success(val dbs: List<DbItem>) : DbUiState
    data class Error(val message: String) : DbUiState
}

sealed interface DbEvent {
    data class OnSearchQueryChanged(val query: String) : DbEvent
    data object Refresh : DbEvent
}

class DbViewModel(
    private val repository: DbRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<DbUiState> = repository.getDbs()
        .combine(_searchQuery) { dbs, query ->
            val filtered = if (query.isBlank()) {
                dbs
            } else {
                dbs.filter { it.name.contains(query, ignoreCase = true) }
            }
            DbUiState.Success(filtered)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DbUiState.Loading
        )

    init {
        refresh()
    }

    fun onEvent(event: DbEvent) {
        when (event) {
            is DbEvent.OnSearchQueryChanged -> _searchQuery.value = event.query
            DbEvent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            repository.syncDbs()
        }
    }

    fun getDbItems(dbId: String): Flow<List<DbSubItem>> {
        return repository.getDbItems(dbId)
    }

    fun syncItemsForDb(dbId: String) {
        viewModelScope.launch {
            repository.syncDbItems(dbId)
        }
    }

    fun getApuResourcesForDbItem(itemId: String): Flow<List<ApuResourceItem>> {
        return repository.getApuResourcesForDbItem(itemId)
    }

    fun syncDbItemResources(itemId: String) {
        viewModelScope.launch {
            repository.syncDbItemResources(itemId)
        }
    }

    fun addResourceToDbItem(itemId: String, resourceId: String, quantity: Double = 1.0) {
        viewModelScope.launch {
            repository.addResourceToDbItem(itemId, resourceId, quantity)
        }
    }
}
