package com.cobalto.features.user.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfession(
    @SerialName("professionId") val professionId: Int? = null,
    @SerialName("name") val name: String
)
