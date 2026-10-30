package com.cobalto.features.resource.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.theme.*
import com.cobalto.core.common.util.pdf.PdfGenerator
import com.cobalto.core.common.util.share.ShareManager
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPreviewScreen(
    resourceIds: List<String>,
    quantities: Map<String, Double> = emptyMap(),
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ResourcesSelectedViewModel = koinViewModel { parametersOf(resourceIds, quantities) },
    pdfGenerator: PdfGenerator = koinInject(),
    shareManager: ShareManager = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val vmQuantities by viewModel.quantities.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vista Previa de Impresión", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (uiState is ResourcesSelectedUiState.Success) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val resources = (uiState as ResourcesSelectedUiState.Success).resources
                        val pdfPath = pdfGenerator.generateResourcesPdf(resources, vmQuantities)
                        if (pdfPath != null) {
                            shareManager.sharePdf(pdfPath, "Cotización de Materiales")
                        }
                    },
                    containerColor = IndustrialOrange,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                    text = { Text("Generar PDF Final") }
                )
            }
        },
        containerColor = Color(0xFFE0E0E0) // Fondo gris para resaltar la "hoja"
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is ResourcesSelectedUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center), color = IndustrialOrange)
                }
                is ResourcesSelectedUiState.Success -> {
                    // Simulación de hoja Carta (Proporción 1:1.29 aproximadamente)
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .widthIn(max = 600.dp) // Limitar ancho para que parezca una hoja
                            .fillMaxHeight()
                            .shadow(8.dp)
                            .background(Color.White),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp)
                        ) {
                            // Cabecera del Preview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "MaterialsSP",
                                        color = IndustrialOrange,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = "Gestión Industrial de Materiales",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "COTIZACIÓN",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = Color.Black, thickness = 2.dp)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Listado de Materiales Seleccionados",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Tabla de materiales (Versión simplificada para el preview)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F5F5))
                                    .padding(8.dp)
                            ) {
                                Text("Material", Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text("Unidad", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text("Cant.", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text("Total", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(state.resources) { material ->
                                    val price = material.price ?: 0.0
                                    val quantity = vmQuantities[material.resourceId] ?: 1.0
                                    val rowTotal = quantity * price
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Text(material.name, Modifier.weight(2f), fontSize = 10.sp, maxLines = 1)
                                        Text(material.unit, Modifier.weight(1f), fontSize = 10.sp)
                                        Text(quantity.toString(), Modifier.weight(0.7f), fontSize = 10.sp)
                                        Text(
                                            text = "$${((rowTotal * 100).toInt() / 100.0)}",
                                            modifier = Modifier.weight(1.2f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IndustrialOrange,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                                        )
                                    }
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Gran Total
                            val grandTotal = state.resources.sumOf { material ->
                                val price = material.price ?: 0.0
                                val quantity = vmQuantities[material.resourceId] ?: 1.0
                                quantity * price
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "TOTAL GENERAL: $${((grandTotal * 100).toInt() / 100.0)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = IndustrialCharcoalDark
                                )
                            }
                        }
                    }
                }
                is ResourcesSelectedUiState.Error -> {
                    Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
