package com.cobalto.features.provider.data.mapper

import com.cobalto.core.database.provider.ProviderEntity
import com.cobalto.features.provider.domain.model.Provider

fun ProviderEntity.toDomain() = Provider(
    providerId = providerId,
    name = name,
    address = address,
    telephone = telephone,
    city = city,
    email = email,
    imagePath = imagePath
)

fun Provider.toEntity() = ProviderEntity(
    providerId = providerId,
    name = name,
    address = address,
    telephone = telephone,
    city = city,
    email = email,
    imagePath = imagePath
)
