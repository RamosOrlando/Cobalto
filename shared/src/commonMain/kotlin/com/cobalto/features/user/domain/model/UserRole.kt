package com.cobalto.features.user.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRole(
    @SerialName("roleId") val roleId: Int? = null,
    @SerialName("name") val name: String
)
