package com.cobalto

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cobalto.features.db.presentation.DbScreen
import org.koin.mp.KoinPlatformTools

@Composable
@Preview
fun App() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        initKoin()
    }

    MaterialTheme {
        DbScreen()
    }
}
