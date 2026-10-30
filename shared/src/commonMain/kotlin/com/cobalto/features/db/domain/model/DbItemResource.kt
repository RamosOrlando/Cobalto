package com.cobalto.features.db.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbItemResource(
    @SerialName("itemId") val itemId: String,
    @SerialName("resourceId") val resourceId: String,
    @SerialName("quantity") val quantity: Double
)
