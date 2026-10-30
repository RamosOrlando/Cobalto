package com.cobalto.features.price_history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.theme.*
import com.cobalto.features.resource.domain.model.ResourceWithPrices
import com.cobalto.features.resource.domain.model.PriceWithProvider
import com.cobalto.core.common.domain.model.Resource
import com.cobalto.features.maker.domain.model.Maker
import com.cobalto.features.provider.domain.model.Provider
import com.cobalto.features.price_history.domain.model.PriceHistory
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PriceHistoryScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PriceHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PriceHistoryScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        isRefreshing = isRefreshing,
        onEvent = { viewModel.onEvent(it) },
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun PriceHistoryScreenContent(
    uiState: PriceHistoryUiState,
    searchQuery: String,
    isRefreshing: Boolean = false,
    onEvent: (PriceHistoryEvent) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
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
                    text = "Historial de Precios",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Seguimiento de cotizaciones y cambios de costos.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                modifier = Modifier.padding(start = 40.dp)
            )
        }

        // Search Bar
        PriceHistorySearchBar(
            query = searchQuery,
            onQueryChange = { onEvent(PriceHistoryEvent.OnSearchQueryChanged(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Body Area
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (val state = uiState) {
                is PriceHistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is PriceHistoryUiState.Success -> {
                    if (state.history.isEmpty()) {
                        if (isRefreshing) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            EmptyHistoryState()
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.history, key = { it.resource.resourceId }) { materialWithPrices ->
                                PriceHistoryItem(resourceWithPrices = materialWithPrices)
                            }
                        }
                    }
                }
                is PriceHistoryUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun PriceHistorySearchBar(
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
                text = "Buscar material o proveedor",
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
fun PriceHistoryItem(
    resourceWithPrices: ResourceWithPrices,
    modifier: Modifier = Modifier
) {
    val pricesByProvider = remember(resourceWithPrices) {
        resourceWithPrices.prices
            .groupBy { it.provider?.providerId }
            .values
            .map { quotes ->
                quotes.sortedByDescending { it.priceHistory.quoteDate }
            }
            .sortedBy { it.firstOrNull()?.provider?.name ?: "Proveedor desconocido" }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(IndustrialShapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), IndustrialShapes.medium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = resourceWithPrices.resource.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            resourceWithPrices.maker.let { maker ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = maker.name,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            if (pricesByProvider.isEmpty()) {
                Text(
                    text = "No hay cotizaciones disponibles",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    pricesByProvider.forEach { providerQuotes ->
                        val firstQuote = providerQuotes.first()
                        Column {
                            // Provider Header
                            Text(
                                text = firstQuote.provider?.name ?: "Proveedor desconocido",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            firstQuote.provider?.address?.let { address ->
                                Text(
                                    text = address,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // All quotes for this provider
                            providerQuotes.forEach { quote ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📅 ${quote.priceHistory.quoteDate}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Bs. ${quote.priceHistory.price}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Historial vacío",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No hay registros de precios aún.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
fun PriceHistoryScreenSuccessPreview() {
    IndustrialTheme {
        PriceHistoryScreenContent(
            uiState = PriceHistoryUiState.Success(
                listOf(
                    ResourceWithPrices(
                        resource = Resource(
                            resourceId = "1",
                            name = "Cemento Holcim Fuerte",
                            unit = "Bulto 50kg",
                            makerId = "MAKER-1",
                            sectionId = "1",
                            specId = null,
                            providerId = null,
                            quoteDate = "2024-05-16"
                        ),
                        maker = Maker("MAKER-1", "Holcim"),
                        prices = listOf(
                            PriceWithProvider(
                                priceHistory = PriceHistory(
                                    historyId = "1",
                                    materialId = "1",
                                    providerId = "1",
                                    price = 185.50,
                                    quoteDate = "2024-05-16",
                                    username = "admin"
                                ),
                                provider = Provider(
                                    providerId = "1",
                                    name = "Materiales del Norte",
                                    city = "Monterrey"
                                )
                            ),
                            PriceWithProvider(
                                priceHistory = PriceHistory(
                                    historyId = "2",
                                    materialId = "1",
                                    providerId = "2",
                                    price = 190.00,
                                    quoteDate = "2024-05-15",
                                    username = "admin"
                                ),
                                provider = Provider(
                                    providerId = "2",
                                    name = "Ferretería El Martillo",
                                    city = "Monterrey"
                                )
                            )
                        )
                    )
                )
            ),
            searchQuery = "",
            onEvent = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun PriceHistoryScreenLoadingPreview() {
    IndustrialTheme {
        PriceHistoryScreenContent(
            uiState = PriceHistoryUiState.Loading,
            searchQuery = "",
            onEvent = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun PriceHistoryScreenErrorPreview() {
    IndustrialTheme {
        PriceHistoryScreenContent(
            uiState = PriceHistoryUiState.Error("Error al cargar el historial."),
            searchQuery = "",
            onEvent = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
fun PriceHistoryScreenEmptyPreview() {
    IndustrialTheme {
        PriceHistoryScreenContent(
            uiState = PriceHistoryUiState.Success(emptyList()),
            searchQuery = "Material inexistente",
            onEvent = {},
            onBackClick = {}
        )
    }
}
