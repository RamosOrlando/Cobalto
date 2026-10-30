package com.cobalto.features.price_history.domain.use_case

import com.cobalto.core.domain.util.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.cobalto.features.resource.domain.model.ResourceWithPrices
import com.cobalto.features.resource.domain.repository.ResourceRepository

class GetPriceHistoryUseCase(
    private val repository: ResourceRepository
) {
    fun executeFlow(query: String = ""): Flow<ResultState<List<ResourceWithPrices>>> {
        return repository.getResourcesFlow(query, null).map { resource ->
            if (resource is ResultState.Success<List<ResourceWithPrices>>) {
                // Ensure we only show materials that actually have prices
                val filtered = resource.data.filter { it.prices.isNotEmpty() }

                // Sort by latest quote date (descending), then by material name (ascending)
                val sorted = filtered.sortedWith(
                    compareByDescending<ResourceWithPrices> {
                        it.prices.maxByOrNull { p -> p.priceHistory.quoteDate }?.priceHistory?.quoteDate ?: ""
                    }.thenBy { it.resource.name }
                )

                ResultState.Success(sorted)
            } else {
                resource
            }
        }
    }
}
