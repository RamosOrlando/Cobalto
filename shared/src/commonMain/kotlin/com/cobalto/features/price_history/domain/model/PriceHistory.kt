package com.cobalto.features.price_history.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PriceHistory(
    @SerialName("historyId") val historyId: String,
    @SerialName("resourceId") val materialId: String,
    @SerialName("providerId") val providerId: String,
    @SerialName("price") val price: Double,
    @SerialName("quoteDate") val quoteDate: String,
    @SerialName("username") val username: String
)
