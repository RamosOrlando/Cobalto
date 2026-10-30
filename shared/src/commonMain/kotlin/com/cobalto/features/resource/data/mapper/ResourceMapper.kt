package com.cobalto.features.resource.data.mapper

import com.cobalto.core.database.resource.ResourceEntity
import com.cobalto.core.common.domain.model.Resource

fun ResourceEntity.toDomain() = Resource(
    resourceId = resourceId,
    name = name,
    unit = unit,
    makerId = makerId,
    sectionId = sectionId,
    specId = specId,
    providerId = providerId,
    price = price,
    quoteDate = quoteDate
)

fun Resource.toEntity() = ResourceEntity(
    resourceId = resourceId,
    name = name,
    unit = unit,
    makerId = makerId,
    sectionId = sectionId,
    specId = specId,
    providerId = providerId,
    price = price,
    quoteDate = quoteDate
)
