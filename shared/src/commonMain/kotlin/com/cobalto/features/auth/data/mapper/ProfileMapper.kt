package com.cobalto.features.auth.data.mapper

import com.cobalto.core.database.auth.ProfileEntity
import com.cobalto.features.auth.domain.model.UserProfile
import com.cobalto.features.auth.domain.model.UserRole

fun ProfileEntity.toDomain() = UserProfile(
    id = id,
    fullName = fullName,
    email = email,
    role = UserRole.valueOf(role)
)

fun UserProfile.toEntity() = ProfileEntity(
    id = id,
    fullName = fullName,
    email = email,
    role = role.name
)
