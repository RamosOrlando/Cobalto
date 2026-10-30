package com.cobalto.features.resource.domain.model

import com.cobalto.features.price_history.domain.model.PriceHistory
import com.cobalto.features.provider.domain.model.Provider
import com.cobalto.features.maker.domain.model.Maker
import com.cobalto.core.common.domain.model.Resource
import kotlinx.serialization.Serializable

@Serializable
data class ResourceWithPrices(
    val resource: Resource,
    val maker: Maker,
    val prices: List<PriceWithProvider>
)

@Serializable
data class PriceWithProvider(
    val priceHistory: PriceHistory,
    val provider: Provider?
)
