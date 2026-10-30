package com.cobalto.core.data.repository

import com.cobalto.core.domain.repository.SyncRepository
import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.category.domain.repository.CategoryRepository
import com.cobalto.features.maker.domain.repository.MakerRepository
import com.cobalto.features.resource.domain.repository.ResourceRepository
import com.cobalto.features.price_history.domain.repository.PriceHistoryRepository
import com.cobalto.features.provider.domain.repository.ProviderRepository
import com.cobalto.features.section.domain.repository.SectionRepository
import com.cobalto.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SyncRepositoryImpl(
    private val categoryRepository: CategoryRepository,
    private val sectionRepository: SectionRepository,
    private val makerRepository: MakerRepository,
    private val providerRepository: ProviderRepository,
    private val resourceRepository: ResourceRepository,
    private val priceHistoryRepository: PriceHistoryRepository,
    private val userRepository: UserRepository
) : SyncRepository {

    override suspend fun performFullSync(): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            println("SyncRepository: Starting full sync...")
            
            // 1. Categories
            categoryRepository.refreshCategories().also { 
                if (it is ResultState.Error) return@withContext ResultState.Error("Sync Categories: ${it.message}")
            }
            
            // 2. Sections
            sectionRepository.refreshSections().also { 
                if (it is ResultState.Error) return@withContext ResultState.Error("Sync Sections: ${it.message}")
            }
            
            // 3. Makers
            makerRepository.refreshMakers().also { 
                if (it is ResultState.Error) return@withContext ResultState.Error("Sync Makers: ${it.message}")
            }
            
            // 4. Providers
            providerRepository.refreshProviders().also { 
                if (it is ResultState.Error) return@withContext ResultState.Error("Sync Providers: ${it.message}")
            }
            
            // 5. Materials
            resourceRepository.refreshResources().also {
                if (it is ResultState.Error) return@withContext ResultState.Error("Sync Materials: ${it.message}")
            }
            
            // 6. PriceHistory
            priceHistoryRepository.refreshPriceHistory().also { 
                if (it is ResultState.Error) return@withContext ResultState.Error("Sync PriceHistory: ${it.message}")
            }

            // 7. User Metadata (Roles, Professions, Plans)
            userRepository.refreshMetadata().also {
                if (it is ResultState.Error) return@withContext ResultState.Error("Sync User Metadata: ${it.message}")
            }
            
            println("SyncRepository: Full sync finished successfully")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            println("SyncRepository: Error during full sync: ${e.message}")
            ResultState.Error(e.message ?: "Unknown sync error")
        }
    }
}
