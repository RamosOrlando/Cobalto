package com.cobalto.features.db.data.mapper

import com.cobalto.core.database.db.DbEntity
import com.cobalto.features.db.domain.model.DbItem

fun DbEntity.toDomain() = DbItem(
    dbId = dbId,
    name = name,
    imagePath = imagePath
)

fun DbItem.toEntity() = DbEntity(
    dbId = dbId,
    name = name,
    imagePath = imagePath
)
