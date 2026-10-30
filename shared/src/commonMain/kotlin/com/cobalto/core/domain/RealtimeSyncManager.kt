package com.cobalto.core.domain

import com.cobalto.features.category.domain.repository.CategoryRepository
import com.cobalto.features.section.domain.repository.SectionRepository
import com.cobalto.features.maker.domain.repository.MakerRepository
import com.cobalto.features.resource.domain.repository.ResourceRepository
import com.cobalto.features.price_history.domain.repository.PriceHistoryRepository
import com.cobalto.features.provider.domain.repository.ProviderRepository
import com.cobalto.core.domain.use_case.PerformFullSyncUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class RealtimeSyncManager(
    private val categoryRepository: CategoryRepository,
    private val sectionRepository: SectionRepository,
    private val makerRepository: MakerRepository,
    private val resourceRepository: ResourceRepository,
    private val providerRepository: ProviderRepository,
    private val priceHistoryRepository: PriceHistoryRepository,
    private val performFullSyncUseCase: PerformFullSyncUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var syncJob: kotlinx.coroutines.Job? = null

    fun startSyncing() {
        stopSyncing()
        syncJob = scope.launch {
            initialSync()
            
            categoryRepository.listenToRealtimeChanges().launchIn(this)
            sectionRepository.listenToRealtimeChanges().launchIn(this)
            makerRepository.listenToRealtimeChanges().launchIn(this)
            resourceRepository.listenToRealtimeChanges().launchIn(this)
            providerRepository.listenToRealtimeChanges().launchIn(this)
            priceHistoryRepository.listenToRealtimeChanges().launchIn(this)
        }
    }

    fun stopSyncing() {
        syncJob?.cancel()
        syncJob = null
    }

    private suspend fun initialSync() {
        println("RealtimeSyncManager: Starting initialSync...")
        val result = performFullSyncUseCase.execute()
        println("RealtimeSyncManager: initialSync result: $result")
    }
}
