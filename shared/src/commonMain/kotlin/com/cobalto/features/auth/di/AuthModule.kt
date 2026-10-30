package com.cobalto.features.auth.di

import com.cobalto.features.auth.data.repository.AuthRepositoryImpl
import com.cobalto.features.auth.domain.repository.AuthRepository
import com.cobalto.features.auth.presentation.LoginViewModel
import com.cobalto.features.auth.presentation.SignUpViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
}
