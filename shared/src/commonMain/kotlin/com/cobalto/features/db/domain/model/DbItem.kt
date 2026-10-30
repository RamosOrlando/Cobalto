package com.cobalto.features.db.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbItem(
    @SerialName("dbId") val dbId: String,
    @SerialName("name") val name: String,
    @SerialName("imagePath") val imagePath: String? = null
)
