package com.cobalto.features.presupuesto.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectModule(
    val moduleId: String,
    val code: String,
    val title: String,
    val orderIndex: Int = 0,
    val items: List<ProjectItem> = emptyList()
) {
    val totalAmount: Double
        get() = items.sumOf { it.totalAmount }
}
