package com.cobalto.core.common.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Resource(
    @SerialName("resourceId") val resourceId: String,
    @SerialName("name") val name: String,
    @SerialName("unit") val unit: String,
    @SerialName("makerId") val makerId: String,
    @SerialName("sectionId") val sectionId: String,
    @SerialName("specId") val specId: String? = null,
    @SerialName("providerId") val providerId: String? = null,
    @SerialName("price") val price: Double? = null,
    @SerialName("quoteDate") val quoteDate: String? = null
)
