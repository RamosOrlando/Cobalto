package com.cobalto.features.section.domain.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.section.domain.model.Section
import kotlinx.coroutines.flow.Flow

interface SectionRepository {
    suspend fun refreshSections(): ResultState<Unit>
    fun getSectionsFlow(): Flow<ResultState<List<Section>>>
    fun listenToRealtimeChanges(): Flow<Unit>
}
