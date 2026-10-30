package com.cobalto.features.user.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionHistory(
    @SerialName("subHistoryId") val subHistoryId: String,
    @SerialName("userId") val userId: String,
    @SerialName("planId") val planId: Int,
    @SerialName("startDate") val startDate: String,
    @SerialName("endDate") val endDate: String,
    @SerialName("state") val state: String,
    @SerialName("pricePaid") val pricePaid: Float,
    @SerialName("discountAmount") val discountAmount: Float
)
