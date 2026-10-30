package com.cobalto.features.resource.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.theme.*
import com.cobalto.core.common.domain.model.Resource
import com.cobalto.core.common.util.formatDateToDisplay
import com.cobalto.core.common.util.getCurrentDate
import com.cobalto.features.auth.domain.model.UserRole
import androidx.compose.ui.tooling.preview.Preview
import com.cobalto.features.maker.domain.model.Maker
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import com.cobalto.features.provider.presentation.BoliviaCity

@Composable
fun ResourceScreen(
    sectionId: String? = null,
    sectionName: String? = null,
    onBackClick: () -> Unit = {},
    onResourcesSelected: (List<String>) -> Unit = {},
    userRole: UserRole? = null,
    quoteModeEnabled: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: ResourceViewModel = koinViewModel()
) {
    LaunchedEffect(quoteModeEnabled) {
        if (!quoteModeEnabled) {
            viewModel.onEvent(ResourceEvent.ClearSelection)
        }
    }

    LaunchedEffect(sectionId) {
        viewModel.onEvent(ResourceEvent.SetSection(sectionId))
    }

    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedIds by viewModel.selectedMaterialIds.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val nextIndex by viewModel.nextIndex.collectAsState()
    val userEmail = viewModel.userEmail
    
    var editingMaterial by remember { mutableStateOf<Resource?>(null) }
    var editingProviderName by remember { mutableStateOf<String?>(null) }
    var deletingMaterial by remember { mutableStateOf<Resource?>(null) }
    var bulkEditingMaterials by remember { mutableStateOf<List<Resource>?>(null) }
    var creatingForMakerMaterial by remember { mutableStateOf<Resource?>(null) }
    var isCreatingMaterial by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    
    LaunchedEffect(uiState) {
        (uiState as? ResourceUiState.Success)?.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "OK",
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(ResourceEvent.ClearError)
        }
    }

    MaterialScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        selectedIds = selectedIds,
        isRefreshing = isRefreshing,
        snackbarHostState = snackbarHostState,
        listState = listState,
        onEvent = { viewModel.onEvent(it) },
        onBackClick = onBackClick,
        onProceed = { onResourcesSelected(selectedIds.toList()) },
        sectionName = sectionName,
        onEditMaterial = { item -> 
            editingMaterial = item.resource
            editingProviderName = item.providerName
        },
        onDeleteMaterial = { item ->
            deletingMaterial = item.resource
        },
        onBulkEdit = { bulkEditingMaterials = it },
        onAddMaterial = { isCreatingMaterial = true },
        onCreateForMaker = { creatingForMakerMaterial = it },
        userRole = userRole,
        quoteModeEnabled = quoteModeEnabled,
        modifier = modifier
    )
    
    editingMaterial?.let { material ->
        val providers = (uiState as? ResourceUiState.Success)?.providers ?: emptyList()
        EditMaterialDialog(
            material = material,
            providers = providers,
            userEmail = userEmail,
            nextIndex = nextIndex,
            onDismiss = { 
                editingMaterial = null
                editingProviderName = null
            },
            onConfirm = { updatedMaterial ->
                viewModel.onEvent(ResourceEvent.UpdateResource(updatedMaterial))
                editingMaterial = null
                editingProviderName = null
            }
        )
    }

    deletingMaterial?.let { material ->
        DeleteMaterialDialog(
            material = material,
            onDismiss = { deletingMaterial = null },
            onConfirm = {
                viewModel.onEvent(ResourceEvent.DeleteResource(material.resourceId))
                deletingMaterial = null
            }
        )
    }

    bulkEditingMaterials?.let { materials ->
        val first = materials.firstOrNull() ?: return@let
        BulkEditMaterialDialog(
            name = first.name,
            unit = first.unit,
            specId = first.specId,
            onDismiss = { bulkEditingMaterials = null },
            onConfirm = { newName, newUnit, newSpecId ->
                val trimmedName = newName.trim()
                val trimmedUnit = newUnit.trim()
                val trimmedSpecId = newSpecId?.trim()?.ifBlank { null }
                
                val updatedList = materials.map { 
                    it.copy(
                        name = trimmedName, 
                        unit = trimmedUnit,
                        specId = trimmedSpecId
                    ) 
                }
                viewModel.onEvent(ResourceEvent.BulkUpdateMaterials(updatedList))
                bulkEditingMaterials = null
            }
        )
    }

    creatingForMakerMaterial?.let { material ->
        val state = uiState as? ResourceUiState.Success
        val makers = state?.makers ?: emptyList()
        val existingMaterials = state?.resources?.map { it.resource } ?: emptyList()
        CreateMaterialForMakerDialog(
            material = material,
            makers = makers,
            existingMaterials = existingMaterials,
            onDismiss = { creatingForMakerMaterial = null },
            onConfirm = { newMaterial ->
                viewModel.onEvent(ResourceEvent.CreateResource(newMaterial))
                creatingForMakerMaterial = null
            }
        )
    }

    if (isCreatingMaterial) {
        val stateSuccess = uiState as? ResourceUiState.Success
        val makers = stateSuccess?.makers ?: emptyList()
        val existingMaterials = stateSuccess?.resources?.map { it.resource } ?: emptyList()
        CreateMaterialDialog(
            sectionId = sectionId ?: "00-00",
            makers = makers,
            existingMaterials = existingMaterials,
            nextIndex = nextIndex,
            onDismiss = { isCreatingMaterial = false },
            onConfirm = { newMaterial ->
                viewModel.onEvent(ResourceEvent.CreateResource(newMaterial))
                isCreatingMaterial = false
            }
        )
    }
}

@Composable
fun MaterialScreenContent(
    uiState: ResourceUiState,
    searchQuery: String,
    selectedIds: Set<String> = emptySet(),
    sectionName: String? = null,
    isRefreshing: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    listState: LazyListState = rememberLazyListState(),
    onEvent: (ResourceEvent) -> Unit,
    onBackClick: () -> Unit = {},
    onProceed: () -> Unit = {},
    onEditMaterial: (ResourceItem) -> Unit = {},
    onDeleteMaterial: (ResourceItem) -> Unit = {},
    onBulkEdit: (List<Resource>) -> Unit = {},
    onAddMaterial: () -> Unit = {},
    onCreateForMaker: (Resource) -> Unit = {},
    userRole: UserRole? = null,
    quoteModeEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (quoteModeEnabled && selectedIds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onProceed,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = { Text("Continuar (${selectedIds.size})") }
                )
            } else if (userRole != UserRole.CLIENT) {
                FloatingActionButton(
                    onClick = onAddMaterial,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Material")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (sectionName != null) "Materiales - $sectionName" else "Materiales",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            MaterialSearchBar(
                query = searchQuery,
                onQueryChange = { onEvent(ResourceEvent.OnSearchQueryChanged(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body Area
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (val state = uiState) {
                    is ResourceUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    is ResourceUiState.Success -> {
                        if (state.resources.isEmpty()) {
                            if (isRefreshing) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                EmptyState()
                            }
                        } else {
                            val groupedMaterials = remember(state.resources) {
                                state.resources.groupBy { it.resource.name + it.resource.unit }
                            }

                            val scope = rememberCoroutineScope()

                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(
                                    start = 16.dp, 
                                    end = 16.dp, 
                                    top = 8.dp, 
                                    bottom =40.dp // Espacio extra para que el FAB no tape el contenido
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                groupedMaterials.forEach { (key, items) ->
                                    val firstItem = items.first()
                                    
                                    item(key = "header_$key") {
                                        MaterialHeaderCard(
                                            name = firstItem.resource.name,
                                            materialId = firstItem.resource.resourceId,
                                            unit = firstItem.resource.unit,
                                            canEdit = userRole != UserRole.CLIENT,
                                            onEditClick = { onBulkEdit(items.map { it.resource }) },
                                            onCreateClick = { onCreateForMaker(firstItem.resource) }
                                        )
                                    }

                                    items(items, key = { it.resource.resourceId }) { materialItem ->
                                        val isSelected = selectedIds.contains(materialItem.resource.resourceId)
                                        MakerCard(
                                            resourceItem = materialItem,
                                            isSelected = isSelected,
                                            canEdit = userRole != UserRole.CLIENT,
                                            onSelect = {
                                                if (quoteModeEnabled) {
                                                    onEvent(
                                                        ResourceEvent.ToggleResourceSelection(it)
                                                    )
                                                }
                                            },
                                            onEdit = { onEditMaterial(materialItem) },
                                            onDelete = { onDeleteMaterial(materialItem) },
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }

                                // Botón "Volver al principio" al final de la lista
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    listState.animateScrollToItem(0)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                                contentColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            shape = IndustrialShapes.small
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VerticalAlignTop,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Volver al principio",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is ResourceUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = { onEvent(ResourceEvent.Refresh) })
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text(
                text = "Buscar Material",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Icono buscar",
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        ),
        singleLine = true,
        shape = IndustrialShapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun MaterialHeaderCard(
    name: String,
    materialId: String? = null,
    unit: String,
    canEdit: Boolean = false,
    onEditClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = IndustrialShapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!materialId.isNullOrBlank()) {
                    Text(
                        text = "ID: $materialId",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            if (unit.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = IndustrialShapes.small,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = unit,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (canEdit) {
                IconButton(
                    onClick = onCreateClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar Fabricante a este Material",
                        tint = IndustrialOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Material",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MakerCard(
    resourceItem: ResourceItem,
    isSelected: Boolean = false,
    canEdit: Boolean = true,
    onSelect: (String) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val material = resourceItem.resource
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(IndustrialShapes.medium)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = IndustrialShapes.medium
            )
            .clickable { onSelect(material.resourceId) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resourceItem.makerName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ID: ${material.resourceId}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " • ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "📅 ${formatDateToDisplay(material.quoteDate)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " • ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = resourceItem.providerName ?: "---",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (material.price != null) "Bs. ${material.price}" else "---",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    if (canEdit) {
                        IconButton(
                            onClick = { onEdit() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDelete() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaterialDialog(
    material: Resource,
    providers: List<com.cobalto.features.provider.domain.model.Provider> = emptyList(),
    userEmail: String? = null,
    nextIndex: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (Resource) -> Unit
) {
    val isPriceCreation = material.price == null
    val isPriceUpdate = material.price != null

    val initialProvider = remember(material.providerId, providers) {
        providers.find { it.providerId == material.providerId }
    }

    var providerSearchQuery by remember {
        mutableStateOf(
            if (initialProvider != null) "${initialProvider.providerId} - ${initialProvider.name}"
            else material.providerId ?: ""
        )
    }
    var selectedProviderId by remember { mutableStateOf<String?>(material.providerId) }
    var providerExpanded by remember { mutableStateOf(false) }

    var priceStr by remember { mutableStateOf(material.price?.toString() ?: "") }
    
    var priceError by remember { mutableStateOf<String?>(null) }
    var providerError by remember { mutableStateOf<String?>(null) }
    
    val quoteDate = remember(material.resourceId) {
        getCurrentDate() // Siempre usamos la fecha actual al guardar un precio
    }

    val dialogTitle = if (isPriceCreation) "Crear Precio" else "Actualizar Precio"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = IndustrialShapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Material: ${material.name}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID: ${material.resourceId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (userEmail != null) {
                            Text(
                                text = "Usuario: $userEmail",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Fecha: ${formatDateToDisplay(quoteDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (isPriceCreation) {
                    var selectedCity by remember { mutableStateOf<BoliviaCity?>(
                        initialProvider?.city?.let { cityStr ->
                            BoliviaCity.entries.find { it.cityName.equals(cityStr, ignoreCase = true) }
                        }
                    ) }
                    var cityExpanded by remember { mutableStateOf(false) }

                    // City Dropdown
                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { cityExpanded = !cityExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCity?.cityName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por Ciudad (Opcional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas las ciudades") },
                                onClick = {
                                    selectedCity = null
                                    cityExpanded = false
                                    providerSearchQuery = ""
                                    selectedProviderId = null
                                }
                            )
                            BoliviaCity.entries.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city.cityName) },
                                    onClick = {
                                        selectedCity = city
                                        cityExpanded = false
                                        providerSearchQuery = ""
                                        selectedProviderId = null
                                    }
                                )
                            }
                        }
                    }

                    val providersInCity = remember(selectedCity, providers) {
                        providers.filter { provider ->
                            selectedCity == null || provider.city.equals(selectedCity?.cityName, ignoreCase = true)
                        }
                    }

                    val filteredProviders = remember(providerSearchQuery, providersInCity) {
                        providersInCity.filter {
                            it.providerId.contains(providerSearchQuery, ignoreCase = true) ||
                                    it.name.contains(providerSearchQuery, ignoreCase = true)
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = providerExpanded,
                        onExpandedChange = { providerExpanded = !providerExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = providerSearchQuery,
                            onValueChange = { query ->
                                providerSearchQuery = query
                                selectedProviderId = providersInCity.find { 
                                    it.providerId.equals(query.trim(), ignoreCase = true) 
                                }?.providerId
                                providerExpanded = true
                                providerError = null
                            },
                            label = { Text("Buscar Proveedor (ID o Nombre)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                            isError = providerError != null,
                            supportingText = if (providerError != null) {
                                { Text(providerError!!, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            singleLine = true
                        )
                        if (filteredProviders.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = providerExpanded,
                                onDismissRequest = { providerExpanded = false }
                            ) {
                                filteredProviders.forEach { provider ->
                                    val displayText = "${provider.providerId} - ${provider.name}"
                                    DropdownMenuItem(
                                        text = { Text(displayText) },
                                        onClick = {
                                            providerSearchQuery = displayText
                                            selectedProviderId = provider.providerId
                                            providerExpanded = false
                                            providerError = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { 
                        priceStr = it
                        priceError = null 
                    },
                    label = { Text("Precio (Bs.)") },
                    isError = priceError != null,
                    supportingText = {
                        if (priceError != null) {
                            Text(text = priceError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanPriceStr = priceStr.trim()
                    val parsedPrice = cleanPriceStr.toDoubleOrNull()
                    
                    val matchedProvider = if (isPriceCreation) {
                        selectedProviderId?.let { id -> providers.find { it.providerId == id } }
                            ?: providers.find { 
                                it.providerId.equals(providerSearchQuery.trim(), ignoreCase = true) ||
                                "${it.providerId} - ${it.name}".equals(providerSearchQuery.trim(), ignoreCase = true)
                            }
                    } else null

                    val finalProviderId = matchedProvider?.providerId

                    if (parsedPrice == null) {
                        priceError = "Introduce un número válido"
                    } else if (isPriceCreation && finalProviderId == null) {
                        providerError = "Selecciona un proveedor de la lista"
                    } else {
                        onConfirm(
                            material.copy(
                                providerId = if (isPriceCreation) finalProviderId else material.providerId,
                                price = parsedPrice,
                                quoteDate = quoteDate
                            )
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMaterialDialog(
    sectionId: String,
    makers: List<com.cobalto.features.maker.domain.model.Maker>,
    existingMaterials: List<Resource> = emptyList(),
    nextIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: (Resource) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var makerSearchQuery by remember { mutableStateOf("") }
    var selectedMakerId by remember { mutableStateOf<String?>(null) }
    var makerExpanded by remember { mutableStateOf(false) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var unitError by remember { mutableStateOf<String?>(null) }
    var makerError by remember { mutableStateOf<String?>(null) }

    val maxExistingIndex = remember(existingMaterials, sectionId) {
        existingMaterials
            .filter { it.sectionId == sectionId }
            .mapNotNull { mat ->
                val parts = mat.resourceId.split("-")
                if (parts.size >= 2) parts[1].toIntOrNull() else null
            }
            .maxOrNull() ?: 0
    }

    val safeIndex = maxOf(nextIndex, maxExistingIndex + 1)
    val correlative = safeIndex.toString().padStart(3, '0')

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Material", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = IndustrialShapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Sección ID: $sectionId",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Correlativo: $correlative",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val previewId = if (selectedMakerId != null) "$sectionId-$correlative-$selectedMakerId" else "$sectionId-$correlative-?"
                        Text(
                            text = "Vista previa ID: $previewId",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = null
                    },
                    label = { Text("Nombre del Material") },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(text = nameError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { 
                        unit = it
                        unitError = null
                    },
                    label = { Text("Unidad") },
                    isError = unitError != null,
                    supportingText = {
                        if (unitError != null) {
                            Text(text = unitError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val filteredMakers = remember(makerSearchQuery, makers) {
                    makers.filter {
                        it.makerId.contains(makerSearchQuery, ignoreCase = true) ||
                                it.name.contains(makerSearchQuery, ignoreCase = true)
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = makerExpanded,
                    onExpandedChange = { makerExpanded = !makerExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = makerSearchQuery,
                        onValueChange = { 
                            makerSearchQuery = it
                            selectedMakerId = null // Reset selection if typing
                            makerExpanded = true 
                        },
                        label = { Text("Buscar Fabricante") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = makerExpanded) },
                        isError = makerError != null,
                        supportingText = if (makerError != null) {
                            { Text(makerError!!, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                        singleLine = true
                    )
                    if (filteredMakers.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = makerExpanded,
                            onDismissRequest = { makerExpanded = false }
                        ) {
                            filteredMakers.forEach { maker ->
                                DropdownMenuItem(
                                    text = { Text("${maker.makerId} - ${maker.name}") },
                                    onClick = {
                                        makerSearchQuery = "${maker.makerId} - ${maker.name}"
                                        selectedMakerId = maker.makerId
                                        makerExpanded = false
                                        makerError = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedUnit = unit.trim()
                    
                    var hasError = false
                    
                    if (trimmedName.isEmpty()) {
                        nameError = "El nombre no puede estar en blanco"
                        hasError = true
                    }
                    
                    if (trimmedUnit.isEmpty()) {
                        unitError = "La unidad no puede estar en blanco"
                        hasError = true
                    }
                    
                    if (selectedMakerId == null) {
                        makerError = "Selecciona un fabricante de la lista"
                        hasError = true
                    }
                    
                    if (!hasError) {
                        var currentCorrelative = safeIndex
                        var generatedId = "$sectionId-${currentCorrelative.toString().padStart(3, '0')}-$selectedMakerId"

                        while (existingMaterials.any { it.resourceId == generatedId }) {
                            currentCorrelative++
                            generatedId = "$sectionId-${currentCorrelative.toString().padStart(3, '0')}-$selectedMakerId"
                        }

                        val newMaterial = Resource(
                            resourceId = generatedId,
                            name = trimmedName,
                            unit = trimmedUnit,
                            makerId = selectedMakerId!!,
                            sectionId = sectionId,
                            specId = null,
                            providerId = null,
                            price = null,
                            quoteDate = null
                        )
                        onConfirm(newMaterial)
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkEditMaterialDialog(
    name: String,
    unit: String,
    specId: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var newName by remember { mutableStateOf(name) }
    var newUnit by remember { mutableStateOf(unit) }
    var newSpecId by remember { mutableStateOf(specId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Material (Grupal)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Esta acción cambiará el nombre, unidad y Spec ID de todos los registros de este material.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newUnit,
                    onValueChange = { newUnit = it },
                    label = { Text("Unidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newSpecId,
                    onValueChange = { newSpecId = it },
                    label = { Text("Spec ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName, newUnit, newSpecId) }
            ) {
                Text("Actualizar Todo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se encontraron materiales",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Intenta ajustar los filtros o la búsqueda",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = IndustrialShapes.medium,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Error de Conexión",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = IndustrialShapes.small
                ) {
                    Text(text = "Reintentar", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMaterialForMakerDialog(
    material: Resource,
    makers: List<Maker>,
    existingMaterials: List<Resource>,
    onDismiss: () -> Unit,
    onConfirm: (Resource) -> Unit
) {
    // Calculamos el ID base (Sección-Correlativo) eliminando el último segmento tras el guion
    val baseId = remember(material.resourceId) {
        material.resourceId.substringBeforeLast("-")
    }
    
    var makerSearchQuery by remember { mutableStateOf("") }
    var selectedMakerId by remember { mutableStateOf<String?>(null) }
    var makerError by remember { mutableStateOf<String?>(null) }
    var makerExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Fabricante a Material", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Información de solo lectura
                OutlinedTextField(
                    value = material.name,
                    onValueChange = {},
                    label = { Text("Nombre (Solo lectura)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                OutlinedTextField(
                    value = material.unit,
                    onValueChange = {},
                    label = { Text("Unidad (Solo lectura)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                
                // Visualización del nuevo ID progresivo
                Text(
                    text = "Nuevo ID: $baseId-${selectedMakerId ?: "?"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = IndustrialOrange,
                    fontWeight = FontWeight.Bold
                )

                // Campo único para el nuevo Maker con búsqueda
                val filteredMakers = remember(makerSearchQuery, makers) {
                    makers.filter {
                        it.makerId.contains(makerSearchQuery, ignoreCase = true) ||
                                it.name.contains(makerSearchQuery, ignoreCase = true)
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = makerExpanded,
                    onExpandedChange = { makerExpanded = !makerExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = makerSearchQuery,
                        onValueChange = { 
                            makerSearchQuery = it
                            selectedMakerId = null
                            makerExpanded = true
                            makerError = null
                        },
                        label = { Text("Buscar Nuevo Fabricante") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = makerExpanded) },
                        isError = makerError != null,
                        supportingText = if (makerError != null) {
                            { Text(makerError!!, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                        singleLine = true
                    )
                    if (filteredMakers.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = makerExpanded,
                            onDismissRequest = { makerExpanded = false }
                        ) {
                            filteredMakers.forEach { maker ->
                                DropdownMenuItem(
                                    text = { Text("${maker.makerId} - ${maker.name}") },
                                    onClick = {
                                        makerSearchQuery = "${maker.makerId} - ${maker.name}"
                                        selectedMakerId = maker.makerId
                                        makerExpanded = false
                                        makerError = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMakerId == null) {
                        makerError = "Selecciona un fabricante de la lista"
                        return@Button
                    }
                    
                    val newId = "$baseId-$selectedMakerId"
                    val alreadyExists = existingMaterials.any { it.resourceId == newId }
                    
                    if (alreadyExists) {
                        makerError = "Ya existe el material con el fabricante seleccionado"
                    } else {
                        onConfirm(
                            material.copy(
                                resourceId = newId,
                                makerId = selectedMakerId!!,
                                price = null,
                                quoteDate = null
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialOrange)
            ) {
                Text("Crear material")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteMaterialDialog(
    material: Resource,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var deleteInputText by remember { mutableStateOf("") }
    val isDeleteEnabled = deleteInputText.trim() == "Borrar"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Eliminar Material", 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "¿Está seguro de que desea eliminar el material '${material.name}'?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Para proceder con el borrado, por favor introduzca la palabra \"Borrar\":",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = deleteInputText,
                    onValueChange = { deleteInputText = it },
                    placeholder = { Text("Escriba \"Borrar\"") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isDeleteEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.5f)
                )
            ) {
                Text("Borrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true, name = "Success")
@Composable
fun MaterialScreenSuccessPreview() {
    IndustrialTheme {
        MaterialScreenContent(
            uiState = ResourceUiState.Success(
                listOf(
                    ResourceItem(
                        resource = Resource(
                            resourceId = "1",
                            name = "Tubo PVC Presión 1/2\"",
                            unit = "Barra",
                            makerId = "MAKER-01",
                            sectionId = "1",
                            specId = null,
                            providerId = null,
                            price = 12.5,
                            quoteDate = "12-06-2026"
                        ),
                        makerName = "Mexichem Amanco",
                        providerName = "Ferretería Central"
                    ),
                    ResourceItem(
                        resource = Resource(
                            resourceId = "2",
                            name = "Codo 90° PVC 1/2\"",
                            unit = "Unidad",
                            makerId = "MAKER-02",
                            sectionId = "1",
                            specId = null,
                            providerId = null,
                            price = 3.75,
                            quoteDate = "25-11-2026"
                        ),
                        makerName = "Pavco Wavin",
                        providerName = "Materiales del Norte"
                    )
                )
            ),
            searchQuery = "",
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun MaterialScreenLoadingPreview() {
    IndustrialTheme {
        MaterialScreenContent(
            uiState = ResourceUiState.Loading,
            searchQuery = "",
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun MaterialScreenErrorPreview() {
    IndustrialTheme {
        MaterialScreenContent(
            uiState = ResourceUiState.Error("Error al cargar materiales"),
            searchQuery = "",
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
fun MaterialScreenEmptyPreview() {
    IndustrialTheme {
        MaterialScreenContent(
            uiState = ResourceUiState.Success(emptyList()),
            searchQuery = "",
            onEvent = {}
        )
    }
}
