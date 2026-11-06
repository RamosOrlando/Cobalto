package com.cobalto.features.decimal_format.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DecimalFormat(
    @SerialName("decimalId") val decimalId: String,
    @SerialName("codigo") val codigo: String,
    @SerialName("name") val name: String,
    @SerialName("number") val number: Int
)
