package com.cobalto.features.provider.domain.use_case

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.provider.domain.model.Provider
import com.cobalto.features.provider.domain.repository.ProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetProvidersUseCase(
    private val repository: ProviderRepository
) {
    suspend fun refresh(): ResultState<Unit> = repository.refreshProviders()
    
    fun executeFlow(query: String = ""): Flow<ResultState<List<Provider>>> {
        return repository.getProvidersFlow().map { resource ->
            if (resource is ResultState.Success<List<Provider>> && query.isNotBlank()) {
                val filtered = resource.data.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            (it.city?.contains(query, ignoreCase = true) == true)
                }
                ResultState.Success(filtered)
            } else {
                resource
            }
        }
    }
}
