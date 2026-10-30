package com.cobalto.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

// Re-export from :core:design-system module for backward compatibility and clean architecture
val IndustrialOrange = com.cobalto.core.designsystem.theme.IndustrialOrange
val IndustrialCharcoalDark = com.cobalto.core.designsystem.theme.IndustrialCharcoalDark
val IndustrialCharcoalMedium = com.cobalto.core.designsystem.theme.IndustrialCharcoalMedium
val IndustrialSteelBlue = com.cobalto.core.designsystem.theme.IndustrialSteelBlue
val IndustrialBackground = com.cobalto.core.designsystem.theme.IndustrialBackground
val IndustrialSurface = com.cobalto.core.designsystem.theme.IndustrialSurface
val IndustrialOutline = com.cobalto.core.designsystem.theme.IndustrialOutline
val IndustrialShapes = com.cobalto.core.designsystem.theme.IndustrialShapes
val IndustrialTypography = com.cobalto.core.designsystem.theme.IndustrialTypography

@Composable
fun IndustrialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    com.cobalto.core.designsystem.theme.IndustrialTheme(
        darkTheme = darkTheme,
        content = content
    )
}
