package com.cobalto.features.presupuesto.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.theme.IndustrialShapes
import com.cobalto.features.presupuesto.domain.model.Project
import com.cobalto.features.presupuesto.domain.model.ProjectItem
import com.cobalto.features.presupuesto.domain.model.ProjectModule
import com.cobalto.features.presupuesto.presentation.components.AddItemDialog
import com.cobalto.features.presupuesto.presentation.components.AddModuleDialog
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectDetailScreen(
    project: Project,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = koinViewModel()
) {
    var showAddModuleDialog by remember { mutableStateOf(false) }
    var selectedModuleForAddItem by remember { mutableStateOf<ProjectModule?>(null) }

    val modules by viewModel.getModulesForProject(project.projectId).collectAsState(initial = emptyList())
    val projectTotal by viewModel.getProjectTotalAmount(project.projectId).collectAsState(initial = 0.0)

    if (showAddModuleDialog) {
        AddModuleDialog(
            onDismiss = { showAddModuleDialog = false },
            onAddModule = { code, title ->
                viewModel.addModule(project.projectId, code, title)
                showAddModuleDialog = false
            }
        )
    }

    selectedModuleForAddItem?.let { mod ->
        AddItemDialog(
            moduleTitle = "${mod.code} ${mod.title}",
            onDismiss = { selectedModuleForAddItem = null },
            onAddItem = { code, name, unit, quantity, unitPrice ->
                viewModel.addItem(
                    projectId = project.projectId,
                    moduleId = mod.moduleId,
                    code = code,
                    name = name,
                    unit = unit,
                    quantity = quantity,
                    unitPrice = unitPrice
                )
                selectedModuleForAddItem = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Bar
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
                    .padding(horizontal = 12.dp, vertical = 8.dp)
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

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Archivo: ${project.filePath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = IndustrialShapes.small
                ) {
                    Text(
                        text = "Total: S/ ${formatDouble(projectTotal)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Módulos e Ítems",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = { showAddModuleDialog = true },
                shape = IndustrialShapes.small,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar Módulo", fontSize = 12.sp)
            }
        }

        // Modules and Items List
        if (modules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No hay módulos agregados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Agrega tu primer módulo (ej: 01.00 ESTRUCTURAS) para comenzar a estructurar el presupuesto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(modules, key = { it.moduleId }) { module ->
                    ModuleCardBlock(
                        module = module,
                        onAddItemClick = { selectedModuleForAddItem = module },
                        onDeleteModule = { viewModel.deleteModule(module.moduleId) },
                        onDeleteItem = { itemId -> viewModel.deleteItem(itemId) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleCardBlock(
    module: ProjectModule,
    onAddItemClick: () -> Unit,
    onDeleteModule: () -> Unit,
    onDeleteItem: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = IndustrialShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Module Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${module.code} ${module.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(S/ ${formatDouble(module.totalAmount)})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAddItemClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar Ítem",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDeleteModule, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar Módulo",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Items Table
            if (module.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin ítems en este módulo. Presiona '+' para agregar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Código", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                        Text("Descripción", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2.2f))
                        Text("Und", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.6f))
                        Text("Metrado", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                        Text("P.Unit", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                        Text("Parcial", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.0f))
                        Spacer(modifier = Modifier.width(24.dp))
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    module.items.forEach { item ->
                        ItemTableRow(
                            item = item,
                            onDeleteItem = { onDeleteItem(item.itemId) }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun ItemTableRow(
    item: ProjectItem,
    onDeleteItem: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.code ?: "-", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f))
        Text(text = item.name, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(2.2f))
        Text(text = item.unit, fontSize = 12.sp, modifier = Modifier.weight(0.6f))
        Text(text = formatDouble(item.quantity), fontSize = 12.sp, modifier = Modifier.weight(0.9f))
        Text(text = "S/ ${formatDouble(item.unitPrice)}", fontSize = 12.sp, modifier = Modifier.weight(0.9f))
        Text(text = "S/ ${formatDouble(item.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1.0f))
        IconButton(onClick = onDeleteItem, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        }
    }
}

private fun formatDouble(value: Double): String {
    val rounded = (value * 100).toLong() / 100.0
    return rounded.toString()
}
