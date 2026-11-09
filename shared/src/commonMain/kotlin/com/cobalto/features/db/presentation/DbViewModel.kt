package com.cobalto.features.db.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.core.database.db_item_resource.DbItemResourceCounts
import com.cobalto.features.db.domain.model.DbItem
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.db.domain.repository.DbRepository
import com.cobalto.features.decimal_format.domain.model.DecimalFormat
import com.cobalto.features.decimal_format.domain.repository.DecimalFormatRepository
import com.cobalto.features.incidence_format.domain.model.IncidenceFormat
import com.cobalto.features.incidence_format.domain.repository.IncidenceFormatRepository
import com.cobalto.features.incidence_format.domain.util.ApuIncidenceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val repository: DbRepository,
    private val decimalFormatRepository: DecimalFormatRepository,
    private val incidenceFormatRepository: IncidenceFormatRepository
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

    val resourceCountsMap: StateFlow<Map<String, DbItemResourceCounts>> = repository.getResourceCountsMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val decimalFormats: StateFlow<List<DecimalFormat>> = decimalFormatRepository.getDecimalFormatsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incidenceFormats: StateFlow<List<IncidenceFormat>> = incidenceFormatRepository.getIncidenceFormatsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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
            decimalFormatRepository.refreshDecimalFormats()
            incidenceFormatRepository.refreshIncidenceFormats()
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

    fun deleteResourceFromDbItem(itemId: String, resourceId: String) {
        viewModelScope.launch {
            repository.deleteResourceFromDbItem(itemId, resourceId)
        }
    }

    fun addDbItem(dbId: String, name: String, code: String?, unit: String) {
        viewModelScope.launch {
            repository.addDbItem(dbId, name, code, unit)
        }
    }

    fun updateDbItemTotal(itemId: String, total: Double) {
        viewModelScope.launch {
            repository.updateDbItemTotal(itemId, total)
        }
    }

    fun refreshDecimalFormats() {
        viewModelScope.launch {
            decimalFormatRepository.refreshDecimalFormats()
        }
    }

    fun saveDecimalFormatsLocally(formats: List<DecimalFormat>) {
        viewModelScope.launch {
            decimalFormatRepository.saveDecimalFormatsLocally(formats)
        }
    }

    fun saveIncidenceFormatsLocally(formats: List<IncidenceFormat>, dbId: String? = null) {
        viewModelScope.launch {
            incidenceFormatRepository.saveIncidenceFormatsLocally(formats)

            if (!dbId.isNullOrBlank()) {
                recalculateAllDbItemTotals(formats, dbId)
            }
        }
    }

    private suspend fun recalculateAllDbItemTotals(
        incidences: List<IncidenceFormat>,
        dbId: String
    ) {
        runCatching {
            val items = repository.getDbItems(dbId).first()
            items.forEach { subItem ->
                val resources = repository.getApuResourcesForDbItem(subItem.itemId).first()

                val matSubtotal = resources
                    .filter { it.sectionId != "Labor-Labor" && it.sectionId != "Equipment-Equipment" }
                    .sumOf { it.totalPrice }

                val moSubtotal = resources
                    .filter { it.sectionId == "Labor-Labor" }
                    .sumOf { it.totalPrice }

                val eqSubtotal = resources
                    .filter { it.sectionId == "Equipment-Equipment" }
                    .sumOf { it.totalPrice }

                val evaluated = ApuIncidenceCalculator.calculateIncidences(
                    subtotalMaterials = matSubtotal,
                    subtotalLabor = moSubtotal,
                    subtotalEquipment = eqSubtotal,
                    incidences = incidences
                )

                val finalIncidence = evaluated.find {
                    it.incidence.codigo.uppercase() == "T-PU" || it.incidence.incidenceId.uppercase() == "M"
                } ?: evaluated.lastOrNull()

                val newTotal = finalIncidence?.calculatedValue ?: (matSubtotal + moSubtotal + eqSubtotal)

                repository.updateDbItemTotal(subItem.itemId, newTotal)
            }
        }
    }
}
