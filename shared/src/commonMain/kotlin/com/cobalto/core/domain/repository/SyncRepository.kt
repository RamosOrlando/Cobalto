package com.cobalto.core.domain.repository

import com.cobalto.core.domain.util.ResultState

interface SyncRepository {
    suspend fun performFullSync(): ResultState<Unit>
}
