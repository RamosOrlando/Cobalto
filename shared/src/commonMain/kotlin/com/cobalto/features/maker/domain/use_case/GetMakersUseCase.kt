package com.cobalto.features.maker.domain.use_case

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.maker.domain.model.Maker
import com.cobalto.features.maker.domain.repository.MakerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetMakersUseCase(
    private val repository: MakerRepository
) {
    suspend fun refresh(): ResultState<Unit> = repository.refreshMakers()
    
    fun executeFlow(query: String = ""): Flow<ResultState<List<Maker>>> {
        return repository.getMakersFlow().map { resource ->
            if (resource is ResultState.Success<List<Maker>> && query.isNotBlank()) {
                val filtered = resource.data.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                ResultState.Success(filtered)
            } else {
                resource
            }
        }
    }
}
