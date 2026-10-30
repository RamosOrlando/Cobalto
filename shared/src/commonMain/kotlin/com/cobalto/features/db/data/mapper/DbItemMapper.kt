package com.cobalto.features.db.data.mapper

import com.cobalto.core.database.db_item.DbItemEntity
import com.cobalto.features.db.domain.model.DbSubItem

fun DbItemEntity.toDomain() = DbSubItem(
    itemId = itemId,
    name = name,
    unit = unit,
    dbId = dbId,
    code = code,
    imagePath = imagePath
)

fun DbSubItem.toEntity() = DbItemEntity(
    itemId = itemId,
    name = name,
    unit = unit,
    dbId = dbId,
    code = code,
    imagePath = imagePath
)
