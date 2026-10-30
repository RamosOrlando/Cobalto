package com.cobalto.features.db.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbSubItem(
    @SerialName("itemId") val itemId: String,
    @SerialName("name") val name: String,
    @SerialName("unit") val unit: String,
    @SerialName("dbId") val dbId: String,
    @SerialName("Code") val code: String? = null,
    @SerialName("imagePath") val imagePath: String? = null
)
