package com.cobalto.features.incidence_format.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.theme.IndustrialShapes
import com.cobalto.features.incidence_format.domain.model.IncidenceFormat

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
