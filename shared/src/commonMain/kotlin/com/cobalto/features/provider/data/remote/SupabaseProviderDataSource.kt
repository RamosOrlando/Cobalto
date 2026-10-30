package com.cobalto.features.provider.data.remote

import com.cobalto.features.provider.domain.model.Provider
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

class SupabaseProviderDataSource(
    private val supabaseClient: io.github.jan.supabase.SupabaseClient
) : ProviderRemoteDataSource {

    override suspend fun getProviders(): List<Provider> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["Provider"]
                .select()
                .decodeList<Provider>()
        } catch (e: Exception) {
            throw Exception("Error fetching from 'provider' table: ${e.message}")
        }
    }

    override suspend fun saveProvider(provider: Provider): Unit = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["Provider"].upsert(provider)
    }

    override fun observeProviders(): Flow<Unit> = callbackFlow {
        // Wait for auth to be initialized to avoid invalid access token error
        supabaseClient.auth.sessionStatus.first { it !is SessionStatus.Initializing }

        // Explicitly connect to realtime to ensure the token is used correctly
        supabaseClient.realtime.connect()

        val channel = supabaseClient.realtime.channel("provider_changes_${Random.nextLong()}")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "Provider"
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
