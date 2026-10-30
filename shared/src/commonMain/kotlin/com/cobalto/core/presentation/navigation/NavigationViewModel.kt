package com.cobalto.core.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.core.domain.RealtimeSyncManager
import com.cobalto.features.auth.domain.repository.AuthRepository
import com.cobalto.features.auth.domain.model.UserRole
import com.cobalto.features.auth.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val authRepository: AuthRepository,
    private val syncManager: RealtimeSyncManager
) : ViewModel() {

    private val _initialScreen = MutableStateFlow<Screen?>(null)
    val initialScreen = _initialScreen.asStateFlow()

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole = _userRole.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.awaitInitialization()
            authRepository.getUserIdFlow().collect { userId ->
                if (userId != null) {
                    fetchProfile()
                    syncManager.startSyncing()
                    // Si ya tenemos una pantalla inicial definida, no la sobreescribimos.
                    // Esto evita saltos de navegación automáticos durante flujos multi-paso como la recuperación de contraseña.
                    if (_initialScreen.value == null) {
                        _initialScreen.value = Screen.Category
                    }
                } else {
                    syncManager.stopSyncing()
                    _userRole.value = null
                    if (_initialScreen.value != Screen.Login) {
                        _initialScreen.value = Screen.Login
                    }
                }
            }
        }
    }

    private suspend fun fetchProfile() {
        authRepository.getCurrentProfile().onSuccess { profile ->
            _userRole.value = profile?.role
        }
    }

    fun onLoginSuccess() {
        viewModelScope.launch {
            fetchProfile()
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
