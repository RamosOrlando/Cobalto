package com.cobalto.features.provider.domain.use_case

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.provider.domain.model.Provider
import com.cobalto.features.provider.domain.repository.ProviderRepository

class SaveProviderUseCase(
    private val repository: ProviderRepository
) {
    suspend fun execute(provider: Provider): ResultState<Unit> {
        return repository.saveProvider(provider)
    }
}
