package com.cobalto.features.incidence_format.data.remote

import com.cobalto.features.incidence_format.domain.model.IncidenceFormat
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SupabaseIncidenceFormatDataSource(
    private val supabaseClient: io.github.jan.supabase.SupabaseClient
) : IncidenceFormatRemoteDataSource {

    override suspend fun getIncidenceFormats(): List<IncidenceFormat> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["IncidenceFormat"]
            .select()
            .decodeList<IncidenceFormat>()
    }
}
