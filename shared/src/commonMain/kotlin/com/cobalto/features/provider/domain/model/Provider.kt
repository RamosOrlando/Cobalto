package com.cobalto.features.provider.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    @SerialName("providerId") val providerId: String,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String? = null,
    @SerialName("telephone") val telephone: String? = null,
    @SerialName("city") val city: String,
    @SerialName("email") val email: String? = null,
    @SerialName("imagePath") val imagePath: String? = null
)
