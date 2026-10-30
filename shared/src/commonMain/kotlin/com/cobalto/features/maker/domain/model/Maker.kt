package com.cobalto.features.maker.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Maker(
    @SerialName("makerId") val makerId: String,
    @SerialName("name") val name: String,
    @SerialName("imagePath") val imagePath: String? = null
)
