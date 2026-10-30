package com.cobalto.features.user.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("userId") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("lastName") val lastName: String,
    @SerialName("email") val email: String,
    @SerialName("roleId") val roleId: Int,
    @SerialName("professionId") val professionId: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("cellphone") val cellphone: Int? = null
)
