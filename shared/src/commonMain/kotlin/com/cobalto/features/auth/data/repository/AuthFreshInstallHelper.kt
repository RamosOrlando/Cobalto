package com.cobalto.features.auth.data.repository

expect suspend fun checkFreshInstallAndClearSessionIfNeeded(signOutAction: suspend () -> Unit)
