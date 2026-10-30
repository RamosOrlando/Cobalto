package com.cobalto.core.domain.use_case

import com.cobalto.core.domain.repository.SyncRepository
import com.cobalto.core.domain.util.ResultState

class PerformFullSyncUseCase(
    private val repository: SyncRepository
) {
    suspend fun execute(): ResultState<Unit> {
        return repository.performFullSync()
    }
}
