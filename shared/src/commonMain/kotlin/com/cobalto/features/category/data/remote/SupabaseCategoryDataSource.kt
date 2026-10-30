package com.cobalto.features.category.data.remote

import com.cobalto.features.category.domain.model.Category
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupabaseCategoryDataSource(
    private val supabaseClient: io.github.jan.supabase.SupabaseClient
) : CategoryRemoteDataSource {

    override suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["Category"]
            .select()
            .decodeList<Category>()
    }

    override fun observeCategories(): Flow<Unit> = callbackFlow {
        // Wait for auth to be initialized to avoid invalid access token error
        supabaseClient.auth.sessionStatus.first { it !is SessionStatus.Initializing }

        // Explicitly connect to realtime to ensure the token is used correctly
        supabaseClient.realtime.connect()

        val channel = supabaseClient.realtime.channel("category_changes_${Random.nextLong()}")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "Category"
        }
        
        val job = flow.onEach {
            trySend(Unit)
        }.launchIn(this)

        channel.subscribe()
        
        awaitClose {
            job.cancel()
            launch {
                channel.unsubscribe()
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }
}
