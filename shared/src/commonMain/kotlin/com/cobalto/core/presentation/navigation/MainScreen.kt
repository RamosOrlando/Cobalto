package com.cobalto.core.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.cobalto.core.presentation.home.HomeScreen
import com.cobalto.core.presentation.navigation.components.AppNavigationDrawerSheet
import com.cobalto.core.presentation.navigation.components.CatalogDetailPlaceholder
import com.cobalto.core.presentation.navigation.components.PlaceholderScreen
import com.cobalto.core.presentation.theme.IndustrialTheme
import com.cobalto.features.auth.domain.model.UserRole
import androidx.compose.foundation.text.BasicTextField
import com.cobalto.features.category.presentation.CategoryScreen
import com.cobalto.features.db.presentation.DbEvent
import com.cobalto.features.db.presentation.DbScreen
import com.cobalto.features.db.presentation.DbViewModel
import com.cobalto.features.maker.presentation.MakerScreen
import com.cobalto.features.price_history.presentation.PriceHistoryScreen
import com.cobalto.features.presupuesto.presentation.BudgetScreen
import com.cobalto.features.presupuesto.presentation.BudgetViewModel
import com.cobalto.features.profile.presentation.ProfileScreen
import com.cobalto.features.provider.presentation.ProviderScreen
import com.cobalto.features.resource.presentation.PrintPreviewScreen
import com.cobalto.features.resource.presentation.ResourceScreen
import com.cobalto.features.resource.presentation.ResourcesSelectedScreen
import com.cobalto.features.section.presentation.SectionScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    initialScreen: Screen = Screen.Home,
    userRole: UserRole? = null,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    val headerState by viewModel.userHeaderState.collectAsState()
    val quoteModeEnabled by viewModel.quoteModeEnabled.collectAsState()

    MainScreenContent(
        headerState = headerState,
        quoteModeEnabled = quoteModeEnabled,
        onToggleQuoteMode = viewModel::toggleQuoteMode,
        onLogout = onLogout,
        initialScreen = initialScreen,
        userRole = userRole,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    headerState: UserHeaderState,
    quoteModeEnabled: Boolean = false,
    onToggleQuoteMode: () -> Unit = {},
    onLogout: () -> Unit = {},
    initialScreen: Screen = Screen.Home,
    userRole: UserRole? = null,
    modifier: Modifier = Modifier
) {
    val backstack = remember { mutableStateListOf<Screen>(initialScreen) }
    val currentScreen = backstack.lastOrNull() ?: initialScreen

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val shouldUseRail = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600)

    val isCatalogFlow = currentScreen is Screen.Materiales ||
            currentScreen is Screen.Category ||
            currentScreen is Screen.Section ||
            currentScreen is Screen.Resource ||
            currentScreen is Screen.Maker ||
            currentScreen is Screen.Provider ||
            currentScreen is Screen.PriceHistory

    val screenTitle = when (currentScreen) {
        is Screen.Home -> "Inicio"
        is Screen.Profile -> "Perfil de Usuario"
        is Screen.Db -> "Base de datos"
        is Screen.Presupuesto -> "Presupuesto"
        is Screen.Materiales, is Screen.Category, is Screen.Section, is Screen.Resource, is Screen.Maker, is Screen.Provider, is Screen.PriceHistory -> "Materiales"
        is Screen.Configuracion -> "Configuración"
        else -> "Cobalto"
    }

    val navItems = remember {
        listOf(
            NavigationItem("Catalogo", Icons.Default.GridView, Screen.Category),
            NavigationItem("Fabricantes", Icons.Default.Business, Screen.Maker),
            NavigationItem("Proveedores", Icons.Default.LocalShipping, Screen.Provider),
            NavigationItem("Historial", Icons.Default.History, Screen.PriceHistory)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = DrawerDefaults.scrimColor,
        modifier = Modifier.fillMaxSize(),
        drawerContent = {
            AppNavigationDrawerSheet(
                currentScreen = currentScreen,
                headerState = headerState,
                onNavigateToScreen = { targetScreen ->
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    handleNavClick(targetScreen, currentScreen, backstack)
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .padding(start = 8.dp, end = 4.dp)
                                .size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menú Principal",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = screenTitle,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        if (currentScreen is Screen.Db) {
                            val dbViewModel: DbViewModel = koinViewModel()
                            val searchQuery by dbViewModel.searchQuery.collectAsState()

                            DbTopAppBarSearch(
                                query = searchQuery,
                                onQueryChange = { dbViewModel.onEvent(DbEvent.OnSearchQueryChanged(it)) }
                            )
                        }

                        if (currentScreen is Screen.Presupuesto) {
                            val budgetViewModel: BudgetViewModel = koinViewModel()
                            var showPresupuestoMenu by remember { mutableStateOf(false) }

                            Box {
                                IconButton(onClick = { showPresupuestoMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Opciones de Presupuesto",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                DropdownMenu(
                                    expanded = showPresupuestoMenu,
                                    onDismissRequest = { showPresupuestoMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Crear proyecto nuevo",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.AddCircleOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPresupuestoMenu = false
                                            budgetViewModel.onShowCreateDialog()
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Recuperar proyecto",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.FolderOpen,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPresupuestoMenu = false
                                        }
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Configurar parámetros",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Calculate,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPresupuestoMenu = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Configurar decimales",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPresupuestoMenu = false
                                        }
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Imprimir reportes",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Print,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPresupuestoMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            },
            bottomBar = {
                if (isCatalogFlow && !shouldUseRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        navItems.forEach { item ->
                            val isSelected = isNavItemSelected(currentScreen, item.screen)
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { handleNavClick(item.screen, currentScreen, backstack) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isCatalogFlow && shouldUseRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        navItems.forEach { item ->
                            val isSelected = isNavItemSelected(currentScreen, item.screen)
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { handleNavClick(item.screen, currentScreen, backstack) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                if (backstack.isNotEmpty()) {
                    NavDisplay(
                        backStack = backstack,
                        modifier = Modifier.weight(1f),
                        onBack = { if (backstack.size > 1) backstack.removeLast() }
                    ) { key ->
                        NavEntry(key) {
                            when (key) {
                                Screen.Home -> HomeScreen(
                                    onNavigateToScreen = { target ->
                                        handleNavClick(target, currentScreen, backstack)
                                    }
                                )
                                Screen.Profile -> ProfileScreen(
                                    headerState = headerState,
                                    onLogout = onLogout
                                )
                                Screen.Category, Screen.Materiales, is Screen.Section, is Screen.Resource -> {
                                    CatalogPane(
                                        screen = key,
                                        shouldUseDualPane = shouldUseRail,
                                        userRole = userRole,
                                        quoteModeEnabled = quoteModeEnabled,
                                        backstack = backstack,
                                        onLogout = onLogout
                                    )
                                }
                                is Screen.ResourcesSelected -> ResourcesSelectedScreen(
                                    resourcesIds = key.resourceIds,
                                    onBackClick = { backstack.removeLast() },
                                    onPrintPreview = { ids, quantities ->
                                        backstack.add(Screen.PrintPreview(ids, quantities))
                                    }
                                )
                                is Screen.PrintPreview -> PrintPreviewScreen(
                                    resourceIds = key.resourceIds,
                                    quantities = key.quantities,
                                    onBackClick = { backstack.removeLast() }
                                )
                                Screen.Maker -> MakerScreen(
                                    onBackClick = {
                                        backstack.clear()
                                        backstack.add(Screen.Materiales)
                                    }
                                )
                                Screen.Provider -> ProviderScreen(
                                    onBackClick = {
                                        backstack.clear()
                                        backstack.add(Screen.Materiales)
                                    }
                                )
                                Screen.PriceHistory -> PriceHistoryScreen(
                                    onBackClick = {
                                        backstack.clear()
                                        backstack.add(Screen.Materiales)
                                    }
                                )
                                Screen.Db -> DbScreen()
                                Screen.Presupuesto -> BudgetScreen()
                                Screen.Configuracion -> PlaceholderScreen("Configuración", Icons.Default.Settings)
                                else -> {
                                    Spacer(Modifier.size(0.dp))
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CatalogPane(
    screen: Screen,
    shouldUseDualPane: Boolean,
    userRole: UserRole?,
    quoteModeEnabled: Boolean,
    backstack: SnapshotStateList<Screen>,
    onLogout: () -> Unit
) {
    if (shouldUseDualPane) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CategoryScreen(
                    onCategoryClick = { id ->
                        if (screen is Screen.Category) {
                            backstack.add(element = Screen.Section(id))
                        } else {
                            backstack.removeAt(backstack.size - 1)
                            backstack.add(element = Screen.Section(id))
                        }
                    },
                    onLogout = onLogout,
                    selectedCategoryId = when (screen) {
                        is Screen.Section -> screen.categoryId
                        is Screen.Resource -> screen.categoryId
                        else -> null
                    },
                    columns = 1
                )
            }
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Box(modifier = Modifier.weight(2.4f).fillMaxHeight()) {
                when (screen) {
                    Screen.Category -> CatalogDetailPlaceholder()
                    is Screen.Section -> SectionScreen(
                        categoryId = screen.categoryId,
                        onSectionClick = { sectionId, sectionName ->
                            backstack.add(element = Screen.Resource(sectionId, sectionName, screen.categoryId))
                        },
                        onBackClick = { backstack.removeAt(backstack.size - 1) }
                    )
                    is Screen.Resource -> ResourceScreen(
                        sectionId = screen.sectionId,
                        sectionName = screen.sectionName,
                        userRole = userRole,
                        quoteModeEnabled = quoteModeEnabled,
                        onBackClick = { backstack.removeAt(backstack.size - 1) },
                        onResourcesSelected = { ids ->
                            backstack.add(element = Screen.ResourcesSelected(ids))
                        }
                    )
                    else -> {
                        Spacer(Modifier.size(0.dp))
                    }
                }
            }
        }
    } else {
        // Single Pane
        when (screen) {
            Screen.Category -> CategoryScreen(
                onCategoryClick = { id -> backstack.add(Screen.Section(id)) },
                onLogout = onLogout
            )
            is Screen.Section -> SectionScreen(
                categoryId = screen.categoryId,
                onSectionClick = { sectionId, sectionName -> 
                    backstack.add(Screen.Resource(sectionId, sectionName, screen.categoryId))
                },
                onBackClick = { backstack.removeLast() }
            )
            is Screen.Resource -> ResourceScreen(
                sectionId = screen.sectionId,
                sectionName = screen.sectionName,
                userRole = userRole,
                quoteModeEnabled = quoteModeEnabled,
                onBackClick = { backstack.removeLast() },
                onResourcesSelected = { ids ->
                    backstack.add(element = Screen.ResourcesSelected(ids))
                }
            )
            else -> {
                Spacer(Modifier.size(0.dp))
            }
        }
    }
}

private data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

@Composable
private fun DbTopAppBarSearch(
    query: String,
    onQueryChange: (String) -> Unit
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .widthIn(min = 150.dp, max = 220.dp)
            .height(36.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = com.cobalto.core.presentation.theme.IndustrialShapes.small
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = com.cobalto.core.presentation.theme.IndustrialShapes.small
            )
            .padding(horizontal = 8.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar BD",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Buscar BD...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    )
}

private fun isNavItemSelected(currentScreen: Screen, targetScreen: Screen): Boolean {
    return when (targetScreen) {
        is Screen.Home -> currentScreen is Screen.Home
        is Screen.Category -> currentScreen is Screen.Category || currentScreen is Screen.Section || currentScreen is Screen.Resource || currentScreen is Screen.Materiales
        is Screen.Maker -> currentScreen is Screen.Maker
        is Screen.Provider -> currentScreen is Screen.Provider
        is Screen.PriceHistory -> currentScreen is Screen.PriceHistory
        is Screen.Db -> currentScreen is Screen.Db
        is Screen.Presupuesto -> currentScreen is Screen.Presupuesto
        is Screen.Materiales -> currentScreen is Screen.Materiales || currentScreen is Screen.Category || currentScreen is Screen.Section || currentScreen is Screen.Resource || currentScreen is Screen.Maker || currentScreen is Screen.Provider || currentScreen is Screen.PriceHistory
        is Screen.Configuracion -> currentScreen is Screen.Configuracion
        else -> currentScreen == targetScreen
    }
}

private fun handleNavClick(targetScreen: Screen, currentScreen: Screen, backstack: MutableList<Screen>) {
    val isSelected = isNavItemSelected(currentScreen, targetScreen)
    if (!isSelected || (targetScreen is Screen.Category && (currentScreen is Screen.Section || currentScreen is Screen.Resource))) {
        backstack.clear()
        backstack.add(targetScreen)
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    IndustrialTheme {
        MainScreenContent(
            headerState = UserHeaderState(
                fullName = "Orlando Ramos Flores",
                roleName = "Administrador",
                planName = "Plan Premium",
                daysRemaining = 15
            ),
            userRole = UserRole.ADMIN
        )
    }
}
