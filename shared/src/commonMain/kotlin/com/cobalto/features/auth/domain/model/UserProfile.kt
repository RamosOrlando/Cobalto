package com.cobalto.features.auth.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String?,
    @SerialName("email") val email: String,
    @SerialName("role") val role: UserRole
)
