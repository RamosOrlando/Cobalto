package com.cobalto.features.category.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.cobalto.core.presentation.theme.*
import com.cobalto.features.category.domain.model.Category
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryScreen(
    onCategoryClick: (String) -> Unit,
    onLogout: () -> Unit = {},
    selectedCategoryId: String? = null,
    columns: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSignedOut by viewModel.isSignedOut.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(CategoryEvent.Refresh)
    }

    LaunchedEffect(isSignedOut) {
        if (isSignedOut) {
            onLogout()
        }
    }

    CategoryScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onEvent = { viewModel.onEvent(it) },
        onCategoryClick = onCategoryClick,
        selectedCategoryId = selectedCategoryId,
        columns = columns,
        modifier = modifier
    )
}

@Composable
fun CategoryScreenContent(
    uiState: CategoryUiState,
    searchQuery: String,
    onEvent: (CategoryEvent) -> Unit,
    onCategoryClick: (String) -> Unit,
    selectedCategoryId: String? = null,
    columns: Int? = null,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // Efecto para centrar la categoría seleccionada al principio
    LaunchedEffect(selectedCategoryId, uiState) {
        if (selectedCategoryId != null && uiState is CategoryUiState.Success) {
            val index = uiState.categories.indexOfFirst { it.categoryId == selectedCategoryId }
            if (index != -1) {
                gridState.animateScrollToItem(index)
            }
        }
    }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val finalColumns = columns ?: with(adaptiveInfo.windowSizeClass) {
        when {
            isWidthAtLeastBreakpoint(840) -> 3
            isWidthAtLeastBreakpoint(600) -> 2
            else -> 1
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Content: Title and Subtitle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Categorías",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Explore nuestra selección de materiales ordenado por categorias.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }

        // Search Bar
        SearchBarSection(
            query = searchQuery,
            onQueryChange = { onEvent(CategoryEvent.OnSearchQueryChanged(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Body Area (State handling)
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (val state = uiState) {
                is CategoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is CategoryUiState.Success -> {
                    if (state.categories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No se encontraron categorías",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Intenta buscar con otros términos",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(finalColumns),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.categories, key = { it.categoryId }) { category ->
                                CategoryCard(
                                    category = category,
                                    onCategoryClick = onCategoryClick,
                                    isSelected = category.categoryId == selectedCategoryId
                                )
                            }
                        }
                    }
                }
                is CategoryUiState.Error -> {
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
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { onEvent(CategoryEvent.Refresh) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = IndustrialShapes.small
                                ) {
                                    Text(text = "Reintentar", color = MaterialTheme.colorScheme.onPrimary)
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
fun SearchBarSection(
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
                text = "Buscar Categoría",
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
fun CategoryCard(
    category: Category,
    onCategoryClick: (String) -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(IndustrialShapes.medium)
            .then(
                if (isSelected) Modifier.border(4.dp, MaterialTheme.colorScheme.primary, IndustrialShapes.medium)
                else Modifier
            )
            .clickable { onCategoryClick(category.categoryId) }
    ) {
        SubcomposeAsyncImage(
            model = category.imagePath,
            contentDescription = category.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onSurface)
                            )
                        )
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 100f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = category.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.description ?: "",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
fun CategoryScreenSuccessPreview() {
    IndustrialTheme {
        CategoryScreenContent(
            uiState = CategoryUiState.Success(
                listOf(
                    Category(
                        categoryId = "1",
                        name = "Construcción",
                        description = "Materiales para obra civil y edificación.",
                        imagePath = ""
                    ),
                    Category(
                        categoryId = "2",
                        name = "Electricidad",
                        description = "Cables, interruptores y componentes eléctricos.",
                        imagePath = ""
                    ),
                    Category(
                        categoryId = "3",
                        name = "Fontanería",
                        description = "Tuberías, grifos y sistemas de riego.",
                        imagePath = ""
                    )
                )
            ),
            searchQuery = "",
            onEvent = {},
            onCategoryClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun CategoryScreenLoadingPreview() {
    IndustrialTheme {
        CategoryScreenContent(
            uiState = CategoryUiState.Loading,
            searchQuery = "",
            onEvent = {},
            onCategoryClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun CategoryScreenErrorPreview() {
    IndustrialTheme {
        CategoryScreenContent(
            uiState = CategoryUiState.Error("No se pudo conectar al servidor."),
            searchQuery = "",
            onEvent = {},
            onCategoryClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
fun CategoryScreenEmptyPreview() {
    IndustrialTheme {
        CategoryScreenContent(
            uiState = CategoryUiState.Success(emptyList()),
            searchQuery = "Material inexistente",
            onEvent = {},
            onCategoryClick = {}
        )
    }
}
