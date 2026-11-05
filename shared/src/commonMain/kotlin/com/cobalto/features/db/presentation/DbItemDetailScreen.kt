package com.cobalto.features.db.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.common.domain.model.Resource
import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.presentation.theme.IndustrialShapes
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.maker.data.remote.MakerRemoteDataSource
import com.cobalto.features.resource.data.remote.ResourceRemoteDataSource
import com.cobalto.features.resource.domain.model.ResourceWithPrices
import com.cobalto.features.resource.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

data class ApuResourceItem(
    val code: String? = null,
    val description: String,
    val unit: String,
    val quantity: Double,
    val unitPrice: Double,
    val sectionId: String? = null
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

enum class ResourceFilterType {
    MATERIALS,
    LABOR,
    EQUIPMENT
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
    var activeFilterType by remember { mutableStateOf<ResourceFilterType?>(null) }
    var allDynamicResources by remember { mutableStateOf<List<ApuResourceItem>>(emptyList()) }

    LaunchedEffect(item.itemId) {
        launch {
            runCatching { resourceRepository.refreshResources() }
            runCatching { viewModel.syncDbItemResources(item.itemId) }
        }

        viewModel.getApuResourcesForDbItem(item.itemId).collectLatest { resources ->
            allDynamicResources = resources
        }
    }

    val materialsList = remember(allDynamicResources) {
        allDynamicResources.filter { it.sectionId != "Labor-Labor" && it.sectionId != "Equipment-Equipment" }
    }

    val manoDeObraList = remember(allDynamicResources) {
        allDynamicResources.filter { it.sectionId == "Labor-Labor" }
    }

    val equiposList = remember(allDynamicResources) {
        allDynamicResources.filter { it.sectionId == "Equipment-Equipment" }
    }

    val apuSections = remember(materialsList, manoDeObraList, equiposList) {
        listOf(
            ApuCategorySection(number = "1.-", title = "MATERIALES", items = materialsList),
            ApuCategorySection(number = "2.-", title = "MANO DE OBRA", items = manoDeObraList),
            ApuCategorySection(number = "3.-", title = "EQUIPO, MAQUINARIA Y HERRAMIENTAS", items = equiposList)
        )
    }

    val grandTotal = remember(apuSections) {
        apuSections.sumOf { it.subtotal }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

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
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Column: APU Table Container
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
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
                                val filterType = when (section.title) {
                                    "MATERIALES" -> ResourceFilterType.MATERIALS
                                    "MANO DE OBRA" -> ResourceFilterType.LABOR
                                    "EQUIPO, MAQUINARIA Y HERRAMIENTAS" -> ResourceFilterType.EQUIPMENT
                                    else -> null
                                }

                                ApuSectionBlock(
                                    section = section,
                                    onAddClick = if (filterType != null) {
                                        { activeFilterType = filterType }
                                    } else null,
                                    onQuantityChange = { resourceItem, newQty ->
                                        if (!resourceItem.code.isNull_orBlank()) {
                                            viewModel.addResourceToDbItem(
                                                itemId = item.itemId,
                                                resourceId = resourceItem.code!!,
                                                quantity = newQty
                                            )
                                        }
                                    },
                                    onDeleteResource = { resourceItem ->
                                        if (!resourceItem.code.isNull_orBlank()) {
                                            viewModel.deleteResourceFromDbItem(
                                                itemId = item.itemId,
                                                resourceId = resourceItem.code!!
                                            )
                                        }
                                    }
                                )
                                HorizontalDivider(thickness = 1.5.dp, color = Color.Black)
                            }

                            // Total Summary Row
                            ApuGrandTotalRow(grandTotal = grandTotal)
                        }
                    }
                }

                // Right Column for Desktop / Wide Screen: Side Panel
                if (isWideScreen && activeFilterType != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = IndustrialShapes.small,
                        modifier = Modifier
                            .width(380.dp)
                            .fillMaxHeight()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = IndustrialShapes.small
                            )
                            .padding(12.dp)
                    ) {
                        AddResourcePanelContent(
                            filterType = activeFilterType!!,
                            onDismiss = { activeFilterType = null },
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

            // Modal Dialog for Mobile / Compact Screen
            if (!isWideScreen && activeFilterType != null) {
                AddResourceDialog(
                    filterType = activeFilterType!!,
                    onDismiss = { activeFilterType = null },
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
}

@Composable
fun AddResourcePanelContent(
    filterType: ResourceFilterType,
    onDismiss: () -> Unit,
    onResourceSelected: (Resource) -> Unit,
    modifier: Modifier = Modifier,
    resourceRepository: ResourceRepository = koinInject(),
    makerRemoteDataSource: MakerRemoteDataSource = koinInject()
) {
    var searchQuery by remember { mutableStateOf("") }
    var resourcesList by remember { mutableStateOf<List<ResourceWithPrices>>(emptyList()) }
    var makersMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    fun filterResources(list: List<ResourceWithPrices>): List<ResourceWithPrices> {
        return when (filterType) {
            ResourceFilterType.MATERIALS -> list.filter {
                it.resource.sectionId != "Equipment-Equipment" && it.resource.sectionId != "Labor-Labor"
            }
            ResourceFilterType.LABOR -> list.filter {
                it.resource.sectionId == "Labor-Labor"
            }
            ResourceFilterType.EQUIPMENT -> list.filter {
                it.resource.sectionId == "Equipment-Equipment"
            }
        }
    }

    val panelTitle = when (filterType) {
        ResourceFilterType.MATERIALS -> "Agregar Material"
        ResourceFilterType.LABOR -> "Agregar Mano de Obra"
        ResourceFilterType.EQUIPMENT -> "Agregar Equipo / Maquinaria"
    }

    LaunchedEffect(filterType) {
        // 1. Fetch Maker names directly from Supabase
        launch {
            runCatching {
                val remoteMakers = makerRemoteDataSource.getMakers()
                makersMap = remoteMakers.associate { it.makerId to it.name }
            }
        }

        // 2. Trigger background refresh
        launch {
            runCatching { resourceRepository.refreshResources() }
        }

        // 3. Observe full ResourceWithPrices
        resourceRepository.getResourcesFlow().collect { result ->
            if (result is ResultState.Success) {
                val filteredLocal = filterResources(result.data)
                resourcesList = filteredLocal
                isLoading = false
            } else if (result is ResultState.Error && resourcesList.isEmpty()) {
                isLoading = false
            }
        }
    }

    val filteredResources = remember(resourcesList, searchQuery, makersMap) {
        if (searchQuery.isBlank()) {
            resourcesList
        } else {
            resourcesList.filter { item ->
                val makerName = makersMap[item.resource.makerId] ?: item.maker.name
                item.resource.name.contains(searchQuery, ignoreCase = true) ||
                        item.resource.unit.contains(searchQuery, ignoreCase = true) ||
                        makerName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Panel Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = panelTitle,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar recurso, unidad o fabricante...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            shape = IndustrialShapes.small
        )

        Spacer(modifier = Modifier.height(12.dp))

        // List
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
                items(filteredResources, key = { it.resource.resourceId }) { item ->
                    val displayPrice = item.resource.price
                        ?: item.prices.firstOrNull()?.priceHistory?.price
                        ?: 0.0

                    val makerName = makersMap[item.resource.makerId]
                        ?.takeIf { it.isNotBlank() }
                        ?: item.maker.name.takeIf { it.isNotBlank() && it != "Desconocido" }
                        ?: item.resource.makerId

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                val selectedResource = item.resource.copy(price = displayPrice)
                                onResourceSelected(selectedResource)
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = item.resource.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Unid: ${item.resource.unit}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )

                                    if (makerName.isNotBlank()) {
                                        Text(
                                            text = "  •  ",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = makerName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = if (displayPrice > 0.0) "${formatNumber(displayPrice)} Bs." else "0.00 Bs.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (displayPrice > 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddResourceDialog(
    filterType: ResourceFilterType,
    onDismiss: () -> Unit,
    onResourceSelected: (Resource) -> Unit,
    resourceRepository: ResourceRepository = koinInject(),
    makerRemoteDataSource: MakerRemoteDataSource = koinInject()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            AddResourcePanelContent(
                filterType = filterType,
                onDismiss = onDismiss,
                onResourceSelected = { resource ->
                    onResourceSelected(resource)
                    onDismiss()
                },
                modifier = Modifier.heightIn(max = 450.dp),
                resourceRepository = resourceRepository,
                makerRemoteDataSource = makerRemoteDataSource
            )
        },
        confirmButton = {}
    )
}

@Composable
fun ApuTableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Descripción",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2.6f)
        )
        VerticalBorder()
        Text(
            text = "Unid",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.6f)
        )
        VerticalBorder()
        Text(
            text = "Cantidad",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.1f)
        )
        VerticalBorder()
        Text(
            text = "Precio Prod.",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.1f)
        )
        VerticalBorder()
        Text(
            text = "Costo Total",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
fun ApuSectionBlock(
    section: ApuCategorySection,
    onAddClick: (() -> Unit)? = null,
    onQuantityChange: ((ApuResourceItem, Double) -> Unit)? = null,
    onDeleteResource: ((ApuResourceItem) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header Row (e.g., 1.- MATERIALES, 2.- MANO DE OBRA, 3.- EQUIPO)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .padding(vertical = 4.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onAddClick != null) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar Insumo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
            }

            Text(
                text = "${section.number} ${section.title}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Subtotal: ${formatNumber(section.subtotal)}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Item Rows with Swipe-to-Reveal Delete Action
        section.items.forEachIndexed { index, item ->
            SwipeableApuItemRow(
                item = item,
                onDeleteClick = {
                    if (onDeleteResource != null) {
                        onDeleteResource(item)
                    }
                },
                onQuantityChange = onQuantityChange
            )

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
fun SwipeableApuItemRow(
    item: ApuResourceItem,
    onDeleteClick: () -> Unit,
    onQuantityChange: ((ApuResourceItem, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val revealThresholdPx = 180f
    val revealedOffsetPx = 180f

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring()
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Red Delete Background Action on the Left
        if (animatedOffset > 10f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(64.dp)
                    .align(Alignment.CenterStart)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable {
                        onDeleteClick()
                        offsetX = 0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar insumo",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Foreground Content Row (Swipes right to reveal the left delete action)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX + dragAmount).coerceIn(0f, revealedOffsetPx)
                            offsetX = newOffset
                        },
                        onDragEnd = {
                            offsetX = if (offsetX >= revealThresholdPx * 0.4f) {
                                revealedOffsetPx
                            } else {
                                0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        }
                    )
                }
                .padding(vertical = 6.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Description
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 14.sp,
                modifier = Modifier
                    .weight(2.6f)
                    .padding(horizontal = 2.dp)
            )

            VerticalBorder()

            // Unit
            Text(
                text = item.unit,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.6f)
            )

            VerticalBorder()

            // Editable Quantity TextField
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (onQuantityChange != null && !item.code.isNull_orBlank()) {
                    QuantityTextField(
                        quantity = item.quantity,
                        onQuantityChange = { newQty ->
                            onQuantityChange(item, newQty)
                        }
                    )
                } else {
                    Text(
                        text = formatNumber(item.quantity, decimals = 3),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            VerticalBorder()

            // Unit Price
            Text(
                text = formatNumber(item.unitPrice),
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1.1f)
                    .padding(end = 2.dp)
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
                    .weight(1.1f)
                    .padding(end = 2.dp)
            )
        }
    }
}

@Composable
fun QuantityTextField(
    quantity: Double,
    onQuantityChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(quantity) {
        mutableStateOf(
            if (quantity % 1.0 == 0.0) quantity.toInt().toString() else quantity.toString()
        )
    }

    BasicTextField(
        value = textValue,
        onValueChange = { newText ->
            if (newText.isEmpty() || newText.matches(Regex("^\\d*\\.?\\d*$"))) {
                textValue = newText
                val parsed = newText.toDoubleOrNull()
                if (parsed != null && parsed >= 0) {
                    onQuantityChange(parsed)
                }
            }
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = IndustrialShapes.small
            )
            .border(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = IndustrialShapes.small
            )
            .padding(horizontal = 2.dp, vertical = 3.dp)
    )
}

@Composable
fun ApuGrandTotalRow(grandTotal: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 10.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "COSTO TOTAL DIRECTO POR UNIDAD:",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = formatNumber(grandTotal),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
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

private fun String?.isNull_orBlank(): Boolean = this == null || this.isBlank()
