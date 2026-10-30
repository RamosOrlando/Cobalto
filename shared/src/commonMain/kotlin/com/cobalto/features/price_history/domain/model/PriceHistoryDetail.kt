package com.cobalto.features.price_history.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PriceHistoryDetail(
    val historyId: String,
    val materialName: String,
    val providerName: String,
    val price: Double,
    val quoteDate: String,
    val unit: String?
)
