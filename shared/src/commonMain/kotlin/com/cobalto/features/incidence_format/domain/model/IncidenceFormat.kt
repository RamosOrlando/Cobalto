package com.cobalto.features.incidence_format.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncidenceFormat(
    @SerialName("incidenceId") val incidenceId: String,
    @SerialName("codigo") val codigo: String,
    @SerialName("name") val name: String,
    @SerialName("percent") val percent: Double,
    @SerialName("formula") val formula: String,
    @SerialName("decimalId") val decimalId: String,
    @SerialName("isVisible") val isVisible: Boolean
)
