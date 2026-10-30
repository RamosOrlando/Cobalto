package com.cobalto.features.user.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPlan(
    @SerialName("planId") val planId: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("price") val price: Float,
    @SerialName("discountPrice") val discountPrice: Float? = null,
    @SerialName("discountStartDate") val discountStartDate: String? = null,
    @SerialName("discountEndDate") val discountEndDate: String? = null,
    @SerialName("durationDays") val durationDays: Int,
    @SerialName("isActive") val isActive: Boolean,
    @SerialName("created_at") val createdAt: String
)
