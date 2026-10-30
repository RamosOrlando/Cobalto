package com.cobalto.features.category.domain.use_case

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.category.domain.model.Category
import com.cobalto.features.category.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetCategoriesUseCase(
    private val repository: CategoryRepository
) {
    suspend fun refresh(): ResultState<Unit> = repository.refreshCategories()
    
    fun executeFlow(query: String = ""): Flow<ResultState<List<Category>>> {
        return repository.getCategoriesFlow().map { resource ->
            if (resource is ResultState.Success<List<Category>> && query.isNotBlank()) {
                val filtered = resource.data.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            (it.description?.contains(query, ignoreCase = true) ?: false)
                }
                ResultState.Success(filtered)
            } else {
                resource
            }
        }
    }
}
