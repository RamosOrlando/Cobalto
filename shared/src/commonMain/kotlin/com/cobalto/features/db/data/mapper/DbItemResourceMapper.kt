package com.cobalto.features.db.data.mapper

import com.cobalto.core.database.db_item_resource.DbItemResourceEntity
import com.cobalto.core.database.db_item_resource.DbItemResourceWithDetails
import com.cobalto.features.db.domain.model.DbItemResource
import com.cobalto.features.db.presentation.ApuResourceItem

fun DbItemResourceEntity.toDomain() = DbItemResource(
    itemId = itemId,
    resourceId = resourceId,
    quantity = quantity
)

fun DbItemResource.toEntity() = DbItemResourceEntity(
    itemId = itemId,
    resourceId = resourceId,
    quantity = quantity
)

fun DbItemResourceWithDetails.toApuResourceItem() = ApuResourceItem(
    code = resourceId,
    description = name,
    unit = unit,
    quantity = quantity,
    unitPrice = price ?: 0.0,
    sectionId = sectionId
)
