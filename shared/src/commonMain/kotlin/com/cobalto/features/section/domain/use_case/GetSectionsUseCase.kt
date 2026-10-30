package com.cobalto.features.section.domain.use_case

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.section.domain.model.Section
import com.cobalto.features.section.domain.repository.SectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSectionsUseCase(
    private val repository: SectionRepository
) {
    suspend fun refresh(): ResultState<Unit> = repository.refreshSections()
    
    fun executeFlow(query: String = "", categoryId: String? = null): Flow<ResultState<List<Section>>> {
        return repository.getSectionsFlow().map { resource ->
            if (resource is ResultState.Success<List<Section>>) {
                var filtered = resource.data
                
                if (categoryId != null) {
                    filtered = filtered.filter { it.categoryId == categoryId }
                }
                
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                }
                ResultState.Success(filtered)
            } else {
                resource
            }
        }
    }
}
