package com.cobalto.core.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.cobalto.features.auth.presentation.LoginScreen
import com.cobalto.features.auth.presentation.SignUpScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    viewModel: NavigationViewModel = koinViewModel()
) {
    val initialScreen by viewModel.initialScreen.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    val backstack = remember { mutableStateListOf<Screen>() }

    LaunchedEffect(initialScreen) {
        initialScreen?.let { screen ->
            val currentScreen = backstack.lastOrNull()
            
            if (backstack.isEmpty()) {
                backstack.add(screen)
            } else {
                val isAtAuthScreen = currentScreen is Screen.Login || currentScreen is Screen.SignUp
                val isTargetAuthScreen = screen is Screen.Login || screen is Screen.SignUp
                
                // Si pasamos de una pantalla de auth (Login/SignUp) a una de contenido (Category, etc) o viceversa
                if (isAtAuthScreen != isTargetAuthScreen) {
                    backstack.clear()
                    backstack.add(screen)
                }
            }
        }
    }

    if (backstack.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavDisplay(
        backStack = backstack,
        onBack = { if (backstack.size > 1) backstack.removeLast() }
    ) { key ->
        NavEntry(key) {
            when (key) {
                is Screen.Login -> LoginScreen(
                    onLoginSuccess = {
                        viewModel.onLoginSuccess()
                        backstack.clear()
                        backstack.add(Screen.Category)
                    },
                    onNavigateToSignUp = {
                        backstack.add(Screen.SignUp)
                    }
                )
                is Screen.SignUp -> SignUpScreen(
                    onBackClick = { backstack.removeLast() },
                    onSignUpSuccess = {
                        viewModel.onLoginSuccess()
                        backstack.clear()
                        backstack.add(Screen.Category)
                    }
                )
                else -> MainScreen(
                    onLogout = {
                        viewModel.onLogout()
                        backstack.clear()
                        backstack.add(Screen.Login)
                    },
                    initialScreen = key,
                    userRole = userRole,
                    modifier = modifier
                )
            }
        }
    }
}
