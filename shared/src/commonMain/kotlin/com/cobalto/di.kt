package com.cobalto

import com.cobalto.core.database.AppDatabase
import com.cobalto.core.database.getRoomDatabase
import com.cobalto.core.network.SupabaseClient
import com.cobalto.core.domain.RealtimeSyncManager
import com.cobalto.core.domain.repository.SyncRepository
import com.cobalto.core.data.repository.SyncRepositoryImpl
import com.cobalto.core.domain.use_case.PerformFullSyncUseCase
import com.cobalto.core.presentation.navigation.MainViewModel
import com.cobalto.core.presentation.navigation.NavigationViewModel
import com.cobalto.features.category.data.remote.CategoryRemoteDataSource
import com.cobalto.features.category.data.remote.SupabaseCategoryDataSource
import com.cobalto.features.category.data.repository.CategoryRepositoryImpl
import com.cobalto.features.category.domain.repository.CategoryRepository
import com.cobalto.features.category.domain.use_case.GetCategoriesUseCase
import com.cobalto.features.category.presentation.CategoryViewModel
import com.cobalto.features.db.data.remote.DbRemoteDataSource
import com.cobalto.features.db.data.remote.SupabaseDbDataSource
import com.cobalto.features.db.data.repository.DbRepositoryImpl
import com.cobalto.features.db.domain.repository.DbRepository
import com.cobalto.features.db.presentation.DbViewModel
import com.cobalto.features.section.data.remote.SectionRemoteDataSource
import com.cobalto.features.section.data.remote.SupabaseSectionDataSource
import com.cobalto.features.section.data.repository.SectionRepositoryImpl
import com.cobalto.features.section.domain.repository.SectionRepository
import com.cobalto.features.section.domain.use_case.GetSectionsUseCase
import com.cobalto.features.section.presentation.SectionViewModel
import com.cobalto.features.maker.data.remote.MakerRemoteDataSource
import com.cobalto.features.maker.data.remote.SupabaseMakerDataSource
import com.cobalto.features.maker.data.repository.MakerRepositoryImpl
import com.cobalto.features.maker.domain.repository.MakerRepository
import com.cobalto.features.maker.domain.use_case.GetMakersUseCase
import com.cobalto.features.maker.domain.use_case.SaveMakerUseCase
import com.cobalto.features.maker.presentation.MakerViewModel
import com.cobalto.features.resource.data.remote.ResourceRemoteDataSource
import com.cobalto.features.resource.data.remote.SupabaseResourceDataSource
import com.cobalto.features.resource.data.repository.ResourceRepositoryImpl
import com.cobalto.features.resource.domain.repository.ResourceRepository
import com.cobalto.features.resource.domain.use_case.GetResourcesUseCase
import com.cobalto.features.resource.presentation.ResourceViewModel
import com.cobalto.features.resource.presentation.ResourcesSelectedViewModel
import com.cobalto.features.price_history.data.remote.PriceHistoryRemoteDataSource
import com.cobalto.features.price_history.data.remote.SupabasePriceHistoryDataSource
import com.cobalto.features.price_history.data.repository.PriceHistoryRepositoryImpl
import com.cobalto.features.price_history.domain.repository.PriceHistoryRepository
import com.cobalto.features.price_history.domain.use_case.GetPriceHistoryUseCase
import com.cobalto.features.price_history.presentation.PriceHistoryViewModel
import com.cobalto.features.provider.data.remote.ProviderRemoteDataSource
import com.cobalto.features.provider.data.remote.SupabaseProviderDataSource
import com.cobalto.features.provider.data.repository.ProviderRepositoryImpl
import com.cobalto.features.provider.domain.repository.ProviderRepository
import com.cobalto.features.provider.domain.use_case.GetProvidersUseCase
import com.cobalto.features.provider.domain.use_case.SaveProviderUseCase
import com.cobalto.features.provider.presentation.ProviderViewModel
import com.cobalto.features.user.data.remote.SupabaseUserDataSource
import com.cobalto.features.user.data.remote.UserRemoteDataSource
import com.cobalto.features.user.data.repository.UserRepositoryImpl
import com.cobalto.features.user.domain.repository.UserRepository
import com.cobalto.features.auth.di.authModule
import com.cobalto.features.profile.di.profileModule
import com.cobalto.features.export.di.exportModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf

val dataModule = module {
    single { SupabaseClient.client }
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().sectionDao() }
    single { get<AppDatabase>().makerDao() }
    single { get<AppDatabase>().resourceDao() }
    single { get<AppDatabase>().providerDao() }
    single { get<AppDatabase>().priceHistoryDao() }
    single { get<AppDatabase>().profileDao() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().dbDao() }
    single { get<AppDatabase>().dbItemDao() }
    single { get<AppDatabase>().dbItemResourceDao() }

    factory<CategoryRemoteDataSource> { SupabaseCategoryDataSource(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get()) }

    factory<DbRemoteDataSource> { SupabaseDbDataSource(get()) }
    single<DbRepository> { DbRepositoryImpl(get(), get(), get(), get()) }

    factory<SectionRemoteDataSource> { SupabaseSectionDataSource(get()) }
    single<SectionRepository> { SectionRepositoryImpl(get(), get()) }

    factory<MakerRemoteDataSource> { SupabaseMakerDataSource(get()) }
    single<MakerRepository> { MakerRepositoryImpl(get(), get()) }

    factory<ResourceRemoteDataSource> { SupabaseResourceDataSource(get()) }
    factory<PriceHistoryRemoteDataSource> { SupabasePriceHistoryDataSource(get()) }

    single<ResourceRepository> {
        ResourceRepositoryImpl(
            resourceDao = get(),
            providerDao = get(),
            priceHistoryDao = get(),
            makerDao = get(),
            makerRemoteDataSource = get(),
            remoteDataSource = get()
        )
    }
    single<PriceHistoryRepository> { PriceHistoryRepositoryImpl(get(), get(), get(), get()) }

    factory<ProviderRemoteDataSource> { SupabaseProviderDataSource(get()) }
    single<ProviderRepository> { ProviderRepositoryImpl(get(), get()) }

    factory<UserRemoteDataSource> { SupabaseUserDataSource(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    single<SyncRepository> { SyncRepositoryImpl(get(), get(), get(), get(), get(), get(), get()) }

    single { RealtimeSyncManager(get(), get(), get(), get(), get(), get(), get()) }
}

val domainModule = module {
    factory { GetCategoriesUseCase(get()) }
    factory { GetSectionsUseCase(get()) }
    factory { GetMakersUseCase(get()) }
    factory { SaveMakerUseCase(get()) }
    factory { GetResourcesUseCase(get()) }
    factory { GetPriceHistoryUseCase(get()) }
    factory { GetProvidersUseCase(get()) }
    factory { SaveProviderUseCase(get()) }
    factory { PerformFullSyncUseCase(get()) }
}

val viewModelModule = module {
    viewModelOf(::CategoryViewModel)
    viewModelOf(::DbViewModel)
    viewModelOf(::SectionViewModel)
    viewModelOf(::MakerViewModel)
    viewModelOf(::ResourceViewModel)
    viewModelOf(::PriceHistoryViewModel)
    viewModelOf(::ProviderViewModel)
    viewModelOf(::NavigationViewModel)
    viewModelOf(::MainViewModel)
    factory { (resourceIds: List<String>, initialQuantities: Map<String, Double>?) ->
        ResourcesSelectedViewModel(resourceIds, initialQuantities ?: emptyMap(), get())
    }
}

expect val platformModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(dataModule, domainModule, viewModelModule, authModule, profileModule, exportModule, platformModule)
    }
}
