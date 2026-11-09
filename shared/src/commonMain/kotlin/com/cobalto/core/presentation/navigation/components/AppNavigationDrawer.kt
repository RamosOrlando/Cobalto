package com.cobalto.core.presentation.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.navigation.Screen
import com.cobalto.core.presentation.navigation.UserHeaderState

data class DrawerItemData(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val targetScreen: Screen,
    val badge: String? = null
)

@Composable
fun AppNavigationDrawerSheet(
    currentScreen: Screen,
    headerState: UserHeaderState,
    onNavigateToScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerItems = listOf(
        DrawerItemData("inicio", "Inicio", Icons.Default.Home, Color(0xFFE91E63), Screen.Home),
        DrawerItemData("base_datos", "Base de datos", Icons.Default.Storage, Color(0xFF2563EB), Screen.Db),
        DrawerItemData("presupuesto", "Presupuesto", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFFF59E0B), Screen.Presupuesto),
        DrawerItemData("materiales", "Materiales", Icons.Default.Construction, Color(0xFF10B981), Screen.Materiales),
        DrawerItemData("configuracion", "Configuración", Icons.Default.Settings, Color(0xFF06B6D4), Screen.Configuracion)
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.width(300.dp).fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header: User Profile Banner
            DrawerHeader(
                headerState = headerState,
                onClick = { onNavigateToScreen(Screen.Profile) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Items List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                drawerItems.forEach { item ->
                    val isSelected = isDrawerItemSelected(currentScreen, item.targetScreen)

                    DrawerItemRow(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigateToScreen(item.targetScreen) }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerHeader(
    headerState: UserHeaderState,
    onClick: () -> Unit = {}
) {
    val name = headerState.fullName.ifBlank { "Orlando Ramos Flores" }
    val initial = name.take(1).uppercase()
    val subtitle = if (headerState.roleName.isNotBlank()) headerState.roleName else "Mi Cobalto"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10B981)) // Vibrant Green Header Background
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6)), // Purple Avatar Circle
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver Perfil",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DrawerItemRow(
    item: DrawerItemData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        Color(0xFFD1E9FF) // Light Blue Pill Container for Active Selection
    } else {
        Color.Transparent
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = item.title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (isSelected) Color(0xFF1E3A8A) else MaterialTheme.colorScheme.onSurface
                )
            }

            if (item.badge != null) {
                Surface(
                    color = Color(0xFF2563EB),
                    shape = CircleShape
                ) {
                    Text(
                        text = item.badge,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun isDrawerItemSelected(currentScreen: Screen, targetScreen: Screen): Boolean {
    return when (targetScreen) {
        is Screen.Home -> currentScreen is Screen.Home
        is Screen.Profile -> currentScreen is Screen.Profile
        is Screen.Db -> currentScreen is Screen.Db
        is Screen.Presupuesto -> currentScreen is Screen.Presupuesto
        is Screen.Materiales -> currentScreen is Screen.Materiales || currentScreen is Screen.Category || currentScreen is Screen.Section || currentScreen is Screen.Resource || currentScreen is Screen.Maker || currentScreen is Screen.Provider || currentScreen is Screen.PriceHistory
        is Screen.Configuracion -> currentScreen is Screen.Configuracion
        else -> currentScreen == targetScreen
    }
}
