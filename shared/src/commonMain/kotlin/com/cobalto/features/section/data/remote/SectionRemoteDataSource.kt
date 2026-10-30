package com.cobalto.features.section.data.remote

import com.cobalto.features.section.domain.model.Section
import kotlinx.coroutines.flow.Flow

interface SectionRemoteDataSource {
    suspend fun getSections(): List<Section>
    fun observeSections(): Flow<Unit>
}
