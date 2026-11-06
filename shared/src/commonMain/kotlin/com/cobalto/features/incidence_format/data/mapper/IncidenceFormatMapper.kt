package com.cobalto.features.incidence_format.data.mapper

import com.cobalto.core.database.incidence_format.IncidenceFormatEntity
import com.cobalto.features.incidence_format.domain.model.IncidenceFormat

fun IncidenceFormatEntity.toDomain() = IncidenceFormat(
    incidenceId = incidenceId,
    codigo = codigo,
    name = name,
    percent = percent,
    formula = formula,
    decimalId = decimalId,
    isVisible = isVisible
)

fun IncidenceFormat.toEntity() = IncidenceFormatEntity(
    incidenceId = incidenceId,
    codigo = codigo,
    name = name,
    percent = percent,
    formula = formula,
    decimalId = decimalId,
    isVisible = isVisible
)
