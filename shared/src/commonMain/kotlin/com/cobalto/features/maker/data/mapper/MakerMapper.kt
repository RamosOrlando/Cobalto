package com.cobalto.features.maker.data.mapper

import com.cobalto.core.database.maker.MakerEntity
import com.cobalto.features.maker.domain.model.Maker

fun MakerEntity.toDomain() = Maker(
    makerId = makerId,
    name = name,
    imagePath = imagePath
)

fun Maker.toEntity() = MakerEntity(
    makerId = makerId,
    name = name,
    imagePath = imagePath
)
