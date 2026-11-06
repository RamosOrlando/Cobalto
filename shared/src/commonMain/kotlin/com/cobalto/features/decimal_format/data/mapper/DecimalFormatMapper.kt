package com.cobalto.features.decimal_format.data.mapper

import com.cobalto.core.database.decimal_format.DecimalFormatEntity
import com.cobalto.features.decimal_format.domain.model.DecimalFormat

fun DecimalFormatEntity.toDomain() = DecimalFormat(
    decimalId = decimalId,
    codigo = codigo,
    name = name,
    number = number
)

fun DecimalFormat.toEntity() = DecimalFormatEntity(
    decimalId = decimalId,
    codigo = codigo,
    name = name,
    number = number
)
