package com.cobalto.core.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.navigation.Screen
import com.cobalto.core.presentation.theme.IndustrialShapes

data class HomeShortcutItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val targetScreen: Screen
)

@Composable
fun HomeScreen(
    onNavigateToScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val shortcuts = listOf(
        HomeShortcutItem(
            title = "Base de Datos",
            description = "Bases de datos sincronizadas desde Supabase y Room, precios y análisis APU.",
            icon = Icons.Default.Storage,
            accentColor = Color(0xFF2563EB),
            targetScreen = Screen.Db
        ),
        HomeShortcutItem(
            title = "Presupuesto",
            description = "Cálculo y gestión de presupuestos de obras e insumos.",
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            accentColor = Color(0xFFF59E0B),
            targetScreen = Screen.Presupuesto
        ),
        HomeShortcutItem(
            title = "Materiales",
            description = "Catálogo industrial completo, fabricantes, proveedores e historial de precios.",
            icon = Icons.Default.Construction,
            accentColor = Color(0xFF10B981),
            targetScreen = Screen.Materiales
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = IndustrialShapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Bienvenido a Cobalto",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona una sección o utiliza los accesos directos principales para comenzar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Text(
            text = "Accesos Directos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )

        // 3 Main Shortcut Cards
        shortcuts.forEach { shortcut ->
            HomeShortcutCard(
                shortcut = shortcut,
                onClick = { onNavigateToScreen(shortcut.targetScreen) }
            )
        }
    }
}

@Composable
fun HomeShortcutCard(
    shortcut: HomeShortcutItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = IndustrialShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = shortcut.accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = shortcut.icon,
                        contentDescription = shortcut.title,
                        tint = shortcut.accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = shortcut.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = shortcut.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
