package com.cobalto.features.presupuesto.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectItem(
    @SerialName("itemId") val itemId: String,
    @SerialName("moduleId") val moduleId: String,
    @SerialName("name") val name: String,
    @SerialName("unit") val unit: String,
    @SerialName("dbId") val dbId: String? = null,
    @SerialName("Code") val code: String? = null,
    @SerialName("imagePath") val imagePath: String? = null,
    @SerialName("unitPrice") val unitPrice: Double = 0.0,
    @SerialName("quantity") val quantity: Double = 0.0,
    @SerialName("totalAmount") val totalAmount: Double = quantity * unitPrice,
    @SerialName("orderIndex") val orderIndex: Int = 0
)
