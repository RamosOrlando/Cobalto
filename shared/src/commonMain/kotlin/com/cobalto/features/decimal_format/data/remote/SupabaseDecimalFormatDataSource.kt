package com.cobalto.features.decimal_format.data.remote

import com.cobalto.features.decimal_format.domain.model.DecimalFormat
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SupabaseDecimalFormatDataSource(
    private val supabaseClient: io.github.jan.supabase.SupabaseClient
) : DecimalFormatRemoteDataSource {

    override suspend fun getDecimalFormats(): List<DecimalFormat> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["DecimalFormat"]
            .select()
            .decodeList<DecimalFormat>()
    }
}
