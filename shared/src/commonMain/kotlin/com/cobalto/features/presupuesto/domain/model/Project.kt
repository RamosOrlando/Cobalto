package com.cobalto.features.presupuesto.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val projectId: String,
    val name: String,
    val description: String? = null,
    val filePath: String,
    val createdAt: String,
    val updatedAt: String,
    val totalAmount: Double = 0.0
)
