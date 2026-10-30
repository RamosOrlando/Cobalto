package com.cobalto.features.db.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.common.domain.model.Resource
import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.presentation.theme.IndustrialShapes
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.resource.data.remote.ResourceRemoteDataSource
import com.cobalto.features.resource.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class ApuResourceItem(
    val code: String? = null,
    val description: String,
    val unit: String,
    val quantity: Double,
    val unitPrice: Double
) {
    val totalPrice: Double get() = quantity * unitPrice
}

data class ApuCategorySection(
    val number: String,
    val title: String,
    val items: List<ApuResourceItem>
) {
    val subtotal: Double get() = items.sumOf { it.totalPrice }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbItemDetailScreen(
    item: DbSubItem,
    viewModel: DbViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    resourceRepository: ResourceRepository = koinInject()
) {
    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var dynamicMaterials by remember { mutableStateOf<List<ApuResourceItem>>(emptyList()) }

    LaunchedEffect(item.itemId) {
        launch {
            runCatching { resourceRepository.refreshResources() }
            runCatching { viewModel.syncDbItemResources(item.itemId) }
        }

        viewModel.getApuResourcesForDbItem(item.itemId).collectLatest { resources ->
            dynamicMaterials = resources
        }
    }

    val manoDeObraList = remember(item.itemId) {
        listOf(
            ApuResourceItem(description = "Especialista / Electricista", unit = "hh", quantity = 0.5, unitPrice = 25.00),
            ApuResourceItem(description = "Peón / Ayudante", unit = "hh", quantity = 0.5, unitPrice = 18.00)
        )
    }

    val equiposList = remember(item.itemId) {
        listOf(
            ApuResourceItem(description = "Herramientas Menores (% MO)", unit = "%", quantity = 0.05, unitPrice = 21.50),
            ApuResourceItem(description = "Equipo de Medición / Multímetro", unit = "he", quantity = 0.20, unitPrice = 10.00)
        )
    }

    val apuSections = remember(dynamicMaterials, manoDeObraList, equiposList) {
        listOf(
            ApuCategorySection(number = "1.-", title = "MATERIALES", items = dynamicMaterials),
            ApuCategorySection(number = "2.-", title = "MANO DE OBRA", items = manoDeObraList),
            ApuCategorySection(number = "3.-", title = "EQUIPO, MAQUINARIA Y HERRAMIENTAS", items = equiposList)
        )
    }

    val grandTotal = remember(apuSections) {
        apuSections.sumOf { it.subtotal }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Código: ${item.code ?: "-"} | Unidad: ${item.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            // Table Sheet Container
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = IndustrialShapes.small,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = IndustrialShapes.small
                    )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row
                    ApuTableHeaderRow()

                    HorizontalDivider(thickness = 2.dp, color = Color.Black)

                    // Render Each Section
                    apuSections.forEach { section ->
                        ApuSectionBlock(
                            section = section,
                            onAddClick = if (section.title == "MATERIALES") {
                                { showAddMaterialDialog = true }
                            } else null
                        )
                        HorizontalDivider(thickness = 1.5.dp, color = Color.Black)
                    }

                    // Total Summary Row
                    ApuGrandTotalRow(grandTotal = grandTotal)
                }
            }
        }

        if (showAddMaterialDialog) {
            AddResourceDialog(
                onDismiss = { showAddMaterialDialog = false },
                onResourceSelected = { resource ->
                    viewModel.addResourceToDbItem(
                        itemId = item.itemId,
                        resourceId = resource.resourceId,
                        quantity = 1.0
                    )
                }
            )
        }
    }
}

@Composable
fun AddResourceDialog(
    onDismiss: () -> Unit,
    onResourceSelected: (Resource) -> Unit,
    resourceRepository: ResourceRepository = koinInject(),
    remoteDataSource: ResourceRemoteDataSource = koinInject()
) {
    var searchQuery by remember { mutableStateOf("") }
    var resourcesList by remember { mutableStateOf<List<Resource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // 1. Direct fetch from Supabase
        launch {
            runCatching {
                val remoteList = remoteDataSource.getResources()
                val filteredRemote = remoteList.filter {
                    it.sectionId != "Equipment-Equipment" && it.sectionId != "Labor-Labor"
                }
                if (filteredRemote.isNotEmpty()) {
                    resourcesList = filteredRemote
                    isLoading = false
                }
            }
        }

        // 2. Trigger refresh in background
        launch {
            runCatching { resourceRepository.refreshResources() }
        }

        // 3. Observe local Room database
        resourceRepository.getAllResourcesFlow().collect { result ->
            if (result is ResultState.Success && result.data.isNotEmpty()) {
                val filteredLocal = result.data.filter {
                    it.sectionId != "Equipment-Equipment" && it.sectionId != "Labor-Labor"
                }
                if (filteredLocal.isNotEmpty()) {
                    resourcesList = filteredLocal
                    isLoading = false
                }
            } else if (result is ResultState.Error && resourcesList.isEmpty()) {
                isLoading = false
            }
        }
    }

    val filteredResources = remember(resourcesList, searchQuery) {
        if (searchQuery.isBlank()) {
            resourcesList
        } else {
            resourcesList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.unit.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar Material",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar recurso...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading && filteredResources.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredResources.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay recursos disponibles",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredResources, key = { it.resourceId }) { resource ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onResourceSelected(resource)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = resource.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Unidad: ${resource.unit}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${formatNumber(resource.price ?: 0.0)} Bs.",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ApuTableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Descripción",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2.8f)
        )
        VerticalBorder()
        Text(
            text = "Unid",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.7f)
        )
        VerticalBorder()
        Text(
            text = "Cantidad",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.0f)
        )
        VerticalBorder()
        Text(
            text = "Precio Prod.",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f)
        )
        VerticalBorder()
        Text(
            text = "Costo Total",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
fun ApuSectionBlock(
    section: ApuCategorySection,
    onAddClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header Row (e.g., 1.- MATERIALES)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onAddClick != null) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar Insumo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = "${section.number} ${section.title}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Subtotal: ${formatNumber(section.subtotal)}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Item Rows
        section.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Description
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp,
                    modifier = Modifier
                        .weight(2.8f)
                        .padding(horizontal = 4.dp)
                )

                VerticalBorder()

                // Unit
                Text(
                    text = item.unit,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(0.7f)
                )

                VerticalBorder()

                // Quantity
                Text(
                    text = formatNumber(item.quantity, decimals = 3),
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(end = 4.dp)
                )

                VerticalBorder()

                // Unit Price
                Text(
                    text = formatNumber(item.unitPrice),
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(end = 4.dp)
                )

                VerticalBorder()

                // Total Cost
                Text(
                    text = formatNumber(item.totalPrice),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(end = 4.dp)
                )
            }

            if (index < section.items.lastIndex) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun ApuGrandTotalRow(grandTotal: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "COSTO TOTAL DIRECTO POR UNIDAD:",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = formatNumber(grandTotal),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun VerticalBorder() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

private fun formatNumber(value: Double, decimals: Int = 2): String {
    val factor = when (decimals) {
        3 -> 1000.0
        else -> 100.0
    }
    val rounded = (value * factor).toInt() / factor
    return rounded.toString()
}
