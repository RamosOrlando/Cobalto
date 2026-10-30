package com.cobalto.features.section.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Section(
    @SerialName("sectionId") val sectionId: String,
    @SerialName("name") val name: String,
    @SerialName("categoryId") val categoryId: String,
    @SerialName("imagePath") val imagePath: String? = null
)
