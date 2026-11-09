package com.cobalto

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cobalto.core.presentation.navigation.NavigationRoot
import com.cobalto.core.presentation.theme.IndustrialTheme
import org.koin.mp.KoinPlatformTools

@Composable
@Preview
fun App() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        initKoin()
    }

    IndustrialTheme {
        NavigationRoot()
    }
}
