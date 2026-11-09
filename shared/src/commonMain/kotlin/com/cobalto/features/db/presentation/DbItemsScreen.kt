package com.cobalto.features.db.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.cobalto.core.database.db_item_resource.DbItemResourceCounts
import com.cobalto.core.presentation.components.LazyColumnFastScroller
import com.cobalto.core.presentation.theme.*
import com.cobalto.features.db.domain.model.DbItem
import com.cobalto.features.db.domain.model.DbSubItem
import com.cobalto.features.decimal_format.domain.model.DecimalFormat
import com.cobalto.features.incidence_format.domain.model.IncidenceFormat
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbItemsScreen(
    db: DbItem,
    viewModel: DbViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var itemsList by remember { mutableStateOf<List<DbSubItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<DbSubItem?>(null) }
    var showCreateItemDialog by remember { mutableStateOf(false) }
    var showDecimalFormatDialog by remember { mutableStateOf(false) }
    var showIncidenceFormatDialog by remember { mutableStateOf(false) }

    val resourceCountsMap by viewModel.resourceCountsMap.collectAsState()
    val decimalFormats by viewModel.decimalFormats.collectAsState()
    val incidenceFormats by viewModel.incidenceFormats.collectAsState()

    LaunchedEffect(db.dbId) {
        viewModel.syncItemsForDb(db.dbId)
        viewModel.getDbItems(db.dbId).collectLatest { items ->
            itemsList = items
            isLoading = false
        }
    }

    if (selectedItem != null) {
        DbItemDetailScreen(
            item = selectedItem!!,
            viewModel = viewModel,
            onBackClick = { selectedItem = null },
            modifier = modifier
        )
    } else {
        val filteredItems = remember(itemsList, searchQuery) {
            if (searchQuery.isBlank()) {
                itemsList
            } else {
                itemsList.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            (it.code?.contains(searchQuery, ignoreCase = true) == true) ||
                            it.unit.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = db.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${filteredItems.size} elementos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        var showSettingsMenu by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = { viewModel.syncItemsForDb(db.dbId) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refrescar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showSettingsMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configuraciones",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSettingsMenu,
                                onDismissRequest = { showSettingsMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Parámetros de cálculo",
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
                                        showSettingsMenu = false
                                        showIncidenceFormatDialog = true
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Número de decimales",
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
                                        showSettingsMenu = false
                                        showDecimalFormatDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateItemDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nuevo ítem"
                    )
                }
            },
            modifier = modifier
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Search Bar WhatsApp Style
                DbItemSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                if (isLoading && filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No se encontraron elementos",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    val listState = rememberLazyListState()

                    LazyColumnFastScroller(
                        listState = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(filteredItems, key = { _, item -> item.itemId }) { index, item ->
                                WhatsAppDbItemRow(
                                    item = item,
                                    counts = resourceCountsMap[item.itemId],
                                    onClick = { selectedItem = item }
                                )
                                if (index < filteredItems.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 72.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showCreateItemDialog) {
                CreateDbItemDialog(
                    onDismiss = { showCreateItemDialog = false },
                    onConfirm = { name, code, unit ->
                        viewModel.addDbItem(
                            dbId = db.dbId,
                            name = name,
                            code = code,
                            unit = unit
                        )
                        showCreateItemDialog = false
                    }
                )
            }

            if (showDecimalFormatDialog) {
                DecimalFormatDialog(
                    initialFormats = decimalFormats,
                    onDismiss = { showDecimalFormatDialog = false },
                    onSave = { updatedList ->
                        viewModel.saveDecimalFormatsLocally(updatedList)
                    }
                )
            }

            if (showIncidenceFormatDialog) {
                IncidenceFormatDialog(
                    initialFormats = incidenceFormats,
                    onDismiss = { showIncidenceFormatDialog = false },
                    onSave = { updatedList ->
                        viewModel.saveIncidenceFormatsLocally(updatedList, db.dbId)
                    }
                )
            }
        }
    }
}

@Composable
fun IncidenceFormatDialog(
    initialFormats: List<IncidenceFormat>,
    onDismiss: () -> Unit,
    onSave: (List<IncidenceFormat>) -> Unit
) {
    val baseFormats = remember(initialFormats) {
        if (initialFormats.isNotEmpty()) {
            initialFormats.sortedBy { it.incidenceId.uppercase() }
        } else {
            listOf(
                IncidenceFormat("A", "SUBT-MAT", "SUBTOTAL MATERIALES", 100.0, "1", "PU-PARCIAL", true),
                IncidenceFormat("B", "SUBT-MO", "SUBTOTAL MANO DE OBRA", 100.0, "1", "PU-PARCIAL", true),
                IncidenceFormat("C", "SUBT-HE", "SUBTOTAL HERRAMIENTAS Y EQUIPO", 100.0, "1", "PU-PARCIAL", true),
                IncidenceFormat("D", "T-MAT", "TOTAL MATERIALES", 100.0, "A", "PU-PARCIAL", true),
                IncidenceFormat("E", "CS", "CARGAS SOCIALES", 55.0, "B", "PU-PARCIAL", true),
                IncidenceFormat("F", "IVA", "IMPUESTOS IVA MANO DE OBRA", 14.94, "B+E", "PU-PARCIAL", true),
                IncidenceFormat("G", "T-MO", "TOTAL MANO DE OBRA", 100.0, "B+E+F", "PU-PARCIAL", true),
                IncidenceFormat("H", "HM", "HERRAMIENTAS MENORES", 5.0, "C", "PU-PARCIAL", true),
                IncidenceFormat("I", "T-HE", "TOTAL HERRAMIENTAS Y EQUIPOS", 100.0, "C+H", "PU-PARCIAL", true),
                IncidenceFormat("J", "GGA", "GASTOS GENERALES Y ADMINISTRATIVOS", 10.0, "D+G+I", "PU-PARCIAL", true),
                IncidenceFormat("K", "UTIL", "UTILIDAD", 10.0, "D+G+I+J", "PU-PARCIAL", true),
                IncidenceFormat("L", "IT", "IMPUESTO A LAS TRANSACCIONES", 3.09, "D+G+I+J+K", "PU-PARCIAL", true),
                IncidenceFormat("M", "T-PU", "TOTAL PRECIO UNITARIO", 100.0, "D+G+I+J+K+L", "PU-PARCIAL", true)
            )
        }
    }

    val editedPercents = remember(baseFormats) {
        mutableStateMapOf<String, String>().apply {
            baseFormats.forEach { format ->
                put(format.incidenceId, format.percent.toString())
            }
        }
    }

    val editedFormulas = remember(baseFormats) {
        mutableStateMapOf<String, String>().apply {
            baseFormats.forEach { format ->
                put(format.incidenceId, format.formula)
            }
        }
    }

    val baseDirectItems = remember(baseFormats) {
        baseFormats.filter { it.formula.trim() == "1" }
    }
    val dependentItems = remember(baseFormats) {
        baseFormats.filter { it.formula.trim() != "1" }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Parámetros de cálculo",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Base direct items A, B, C legend
                if (baseDirectItems.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = IndustrialShapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Bases de Cálculo Directo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            baseDirectItems.forEach { baseItem ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "[${baseItem.incidenceId}]",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = baseItem.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Dependent Incidence Formats (Formula & Percent Editable)
                if (dependentItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Porcentajes e Incidencias Indirectas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        dependentItems.forEach { format ->
                            IncidenceFormatRow(
                                format = format,
                                percentTextValue = editedPercents[format.incidenceId] ?: format.percent.toString(),
                                formulaTextValue = editedFormulas[format.incidenceId] ?: format.formula,
                                onPercentValueChange = { newText ->
                                    editedPercents[format.incidenceId] = newText
                                },
                                onFormulaValueChange = { newFormula ->
                                    editedFormulas[format.incidenceId] = newFormula
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedList = baseFormats.map { format ->
                        if (format.formula.trim() == "1") {
                            format
                        } else {
                            val percentText = editedPercents[format.incidenceId]
                            val parsedPercent = percentText?.toDoubleOrNull() ?: format.percent
                            val formulaText = editedFormulas[format.incidenceId]
                            val validFormula = if (!formulaText.isNullOrBlank()) formulaText.trim() else format.formula
                            format.copy(
                                percent = parsedPercent,
                                formula = validFormula
                            )
                        }
                    }
                    onSave(updatedList)
                    onDismiss()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun IncidenceFormatRow(
    format: IncidenceFormat,
    percentTextValue: String,
    formulaTextValue: String,
    onPercentValueChange: (String) -> Unit,
    onFormulaValueChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: [ID Badge] + Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "[${format.incidenceId}]",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = format.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Input Fields Row: Formula TextField + Percent TextField
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Editable Formula Field
                OutlinedTextField(
                    value = formulaTextValue,
                    onValueChange = { input ->
                        onFormulaValueChange(input.uppercase())
                    },
                    label = { Text("Fórmula", fontSize = 10.sp) },
                    placeholder = { Text("Ej: B+E") },
                    modifier = Modifier.weight(1.3f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    shape = IndustrialShapes.small
                )

                // Editable Percent Field
                OutlinedTextField(
                    value = percentTextValue,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onPercentValueChange(input)
                        }
                    },
                    label = { Text("Porcentaje (%)", fontSize = 10.sp) },
                    placeholder = { Text("%") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    shape = IndustrialShapes.small
                )
            }
        }
    }
}

@Composable
fun DecimalFormatDialog(
    initialFormats: List<DecimalFormat>,
    onDismiss: () -> Unit,
    onSave: (List<DecimalFormat>) -> Unit
) {
    val baseFormats = remember(initialFormats) {
        if (initialFormats.isNotEmpty()) {
            initialFormats
        } else {
            listOf(
                DecimalFormat(decimalId = "df_1", codigo = "PU_PRICE", name = "Precio Unitario Insumo", number = 2),
                DecimalFormat(decimalId = "df_2", codigo = "PU_QTY", name = "Cantidad Insumo", number = 2),
                DecimalFormat(decimalId = "df_3", codigo = "PU_TOTAL", name = "Costo Parcial APU", number = 2),
                DecimalFormat(decimalId = "df_4", codigo = "PRES_PRICE", name = "Precio Unitario Rubro", number = 2),
                DecimalFormat(decimalId = "df_5", codigo = "PRES_QTY", name = "Cantidad Rubro", number = 2),
                DecimalFormat(decimalId = "df_6", codigo = "PRES_TOTAL", name = "Monto Total Presupuesto", number = 2)
            )
        }
    }

    val editedNumbers = remember(baseFormats) {
        mutableStateMapOf<String, String>().apply {
            baseFormats.forEach { format ->
                val num = if (format.number in 1..5) format.number else 2
                put(format.decimalId, num.toString())
            }
        }
    }

    val puFormats = remember(baseFormats) {
        baseFormats.filter { it.codigo.uppercase().startsWith("PU") }
    }
    val budgetFormats = remember(baseFormats) {
        baseFormats.filter { !it.codigo.uppercase().startsWith("PU") }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Número de decimales",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Precios Unitarios
                if (puFormats.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Precios Unitarios",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

                        puFormats.forEach { format ->
                            DecimalFormatRow(
                                format = format,
                                textValue = editedNumbers[format.decimalId] ?: "2",
                                onValueChange = { newText ->
                                    editedNumbers[format.decimalId] = newText
                                }
                            )
                        }
                    }
                }

                // Section 2: Presupuesto
                if (budgetFormats.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Presupuesto",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

                        budgetFormats.forEach { format ->
                            DecimalFormatRow(
                                format = format,
                                textValue = editedNumbers[format.decimalId] ?: "2",
                                onValueChange = { newText ->
                                    editedNumbers[format.decimalId] = newText
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedList = baseFormats.map { format ->
                        val text = editedNumbers[format.decimalId]
                        val parsed = text?.toIntOrNull()
                        val validNum = if (parsed != null && parsed in 1..5) parsed else 2
                        format.copy(number = validNum)
                    }
                    onSave(updatedList)
                    onDismiss()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DecimalFormatRow(
    format: DecimalFormat,
    textValue: String,
    onValueChange: (String) -> Unit
) {
    val currentNum = textValue.toIntOrNull() ?: 2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = format.name,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Código: ${format.codigo}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DecimalStepperField(
            value = textValue,
            onValueChange = onValueChange,
            onIncrement = {
                val next = (currentNum + 1).coerceAtMost(5)
                onValueChange(next.toString())
            },
            onDecrement = {
                val prev = (currentNum - 1).coerceAtLeast(1)
                onValueChange(prev.toString())
            },
            enabledUp = currentNum < 5,
            enabledDown = currentNum > 1
        )
    }
}

@Composable
fun DecimalStepperField(
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    enabledUp: Boolean,
    enabledDown: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = IndustrialShapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                shape = IndustrialShapes.small
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(44.dp)
        ) {
            // Number Input Field
            BasicTextField(
                value = value,
                onValueChange = { input ->
                    val digit = input.filter { it.isDigit() }
                    if (digit.isEmpty()) {
                        onValueChange("")
                    } else {
                        val num = digit.takeLast(1).toIntOrNull() ?: 2
                        val clamped = num.coerceIn(1, 5)
                        onValueChange(clamped.toString())
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .width(40.dp)
                    .padding(horizontal = 2.dp)
            )

            // Vertical Separator Line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )

            // Up / Down Stepper Buttons Column
            Column(
                modifier = Modifier
                    .width(30.dp)
                    .fillMaxHeight()
            ) {
                // Up Arrow
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            if (enabledUp) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent
                        )
                        .clickable(enabled = enabledUp, onClick = onIncrement),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Incrementar",
                        tint = if (enabledUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Horizontal Separator Line
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )

                // Down Arrow
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            if (enabledDown) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent
                        )
                        .clickable(enabled = enabledDown, onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrementar",
                        tint = if (enabledDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateDbItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String?, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("unid") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Crear Nuevo Ítem",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Ítem *") },
                    placeholder = { Text("Ej: Excavación manual en terreno semiduro") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = IndustrialShapes.small
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código (Opcional)") },
                    placeholder = { Text("Ej: EXT-001") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = IndustrialShapes.small
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unidad de Medida *") },
                    placeholder = { Text("Ej: m3, m2, unid") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = IndustrialShapes.small
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), code.trim().ifBlank { null }, unit.trim())
                    }
                },
                enabled = name.isNotBlank()
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

@Composable
fun WhatsAppDbItemRow(
    item: DbSubItem,
    counts: DbItemResourceCounts?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // WhatsApp Circular Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imagePath.isNull_orEmpty()) {
                SubcomposeAsyncImage(
                    model = item.imagePath,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Text(
                            text = item.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                )
            } else {
                Text(
                    text = item.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // WhatsApp Style Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Fila 1: Nombre del Ítem solo de extremo a extremo
            Text(
                text = item.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Fila 2: Izquierda = Código + Unidad + Iconos y Cantidades | Derecha = Costo Total en Bs.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lado Izquierdo: Código Chip + Unidad + Iconos de Recursos y Cantidades
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (!item.code.isNull_orEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = item.code ?: "",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = "Unid: ${item.unit}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Iconos y cantidades de recursos seguidos directamente de la unidad
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Materiales (Regla)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = "Materiales",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${counts?.materialsCount ?: 0}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 2. Mano de Obra (Persona)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Mano de Obra",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${counts?.laborCount ?: 0}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 3. Maquinaria / Equipo (Maquinaria)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PrecisionManufacturing,
                                contentDescription = "Maquinaria",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${counts?.equipmentCount ?: 0}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Lado Derecho de la Fila 2: Costo Total en Bs.
                Text(
                    text = "${formatNumber(item.total ?: 0.0)} Bs.",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DbItemSearchBar(
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
                text = "Buscar por nombre o código...",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        singleLine = true,
        shape = IndustrialShapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
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

private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()
