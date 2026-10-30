package com.cobalto.features.auth.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("admin")
    ADMIN,
    @SerialName("quoter")
    QUOTER,
    @SerialName("client")
    CLIENT
}
