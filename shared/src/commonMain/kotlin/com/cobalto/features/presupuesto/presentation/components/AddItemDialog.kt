package com.cobalto.features.presupuesto.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cobalto.core.presentation.theme.IndustrialShapes

@Composable
fun AddItemDialog(
    moduleTitle: String,
    onDismiss: () -> Unit,
    onAddItem: (code: String?, name: String, unit: String, quantity: Double, unitPrice: Double) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("m3") }
    var quantityText by remember { mutableStateOf("") }
    var unitPriceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Agregar Ítem",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Módulo: $moduleTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código de Ítem (Opcional)") },
                    placeholder = { Text("Ej. 01.01.01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShapes.small
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre / Ítem *") },
                    placeholder = { Text("Ej. Concreto F'c=210 kg/cm2 en columnas") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShapes.small
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad *") },
                        placeholder = { Text("m3") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = IndustrialShapes.small
                    )

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Metrado *") },
                        placeholder = { Text("25.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = IndustrialShapes.small
                    )

                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text("P. Unit. *") },
                        placeholder = { Text("380.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        shape = IndustrialShapes.small
                    )
                }
            }
        },
        confirmButton = {
            val qty = quantityText.toDoubleOrNull() ?: 0.0
            val price = unitPriceText.toDoubleOrNull() ?: 0.0
            val isValid = name.isNotBlank() && unit.isNotBlank() && qty > 0.0

            Button(
                onClick = {
                    if (isValid) {
                        onAddItem(code.ifBlank { null }, name, unit, qty, price)
                    }
                },
                enabled = isValid,
                shape = IndustrialShapes.small
            ) {
                Text("Agregar Ítem")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = IndustrialShapes.medium,
        containerColor = MaterialTheme.colorScheme.surface
    )
}
