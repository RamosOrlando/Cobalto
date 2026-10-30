package com.cobalto.features.price_history.data.mapper

import com.cobalto.core.database.price_history.PriceHistoryEntity
import com.cobalto.features.price_history.domain.model.PriceHistory

fun PriceHistoryEntity.toDomain() = PriceHistory(
    historyId = historyId,
    materialId = materialId,
    providerId = providerId,
    price = price,
    quoteDate = quoteDate,
    username = username
)

fun PriceHistory.toEntity() = PriceHistoryEntity(
    historyId = historyId,
    materialId = materialId,
    providerId = providerId,
    price = price,
    quoteDate = quoteDate,
    username = username
)
