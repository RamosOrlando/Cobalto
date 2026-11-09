package com.cobalto.features.presupuesto.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cobalto.core.presentation.theme.IndustrialShapes

@Composable
fun AddModuleDialog(
    onDismiss: () -> Unit,
    onAddModule: (code: String, title: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar Módulo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código de Módulo *") },
                    placeholder = { Text("Ej. 01.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShapes.small
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Módulo *") },
                    placeholder = { Text("Ej. ESTRUCTURAS") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = IndustrialShapes.small
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && title.isNotBlank()) {
                        onAddModule(code, title)
                    }
                },
                enabled = code.isNotBlank() && title.isNotBlank(),
                shape = IndustrialShapes.small
            ) {
                Text("Agregar")
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
