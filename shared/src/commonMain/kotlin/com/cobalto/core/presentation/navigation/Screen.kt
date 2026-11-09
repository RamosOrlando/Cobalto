package com.cobalto.core.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object SignUp : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object Category : Screen
    
    @Serializable
    data object Maker : Screen

    @Serializable
    data object Provider : Screen
    
    @Serializable
    data class Section(val categoryId: String) : Screen
    
    @Serializable
    data class Resource(val sectionId: String? = null, val sectionName: String? = null, val categoryId: String? = null) : Screen

    @Serializable
    data class ResourcesSelected(val resourceIds: List<String>) : Screen

    @Serializable
    data class PrintPreview(
        val resourceIds: List<String>,
        val quantities: Map<String, Double> = emptyMap()
    ) : Screen
    
    @Serializable
    data object PriceHistory : Screen

    @Serializable
    data object Db : Screen

    @Serializable
    data object Presupuesto : Screen

    @Serializable
    data object Materiales : Screen

    @Serializable
    data object Configuracion : Screen
}
