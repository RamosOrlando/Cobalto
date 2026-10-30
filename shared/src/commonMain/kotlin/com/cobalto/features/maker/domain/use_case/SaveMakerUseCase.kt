package com.cobalto.features.maker.domain.use_case

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.maker.domain.model.Maker
import com.cobalto.features.maker.domain.repository.MakerRepository

class SaveMakerUseCase(
    private val repository: MakerRepository
) {
    suspend fun execute(maker: Maker): ResultState<Unit> {
        return repository.saveMaker(maker)
    }
}
