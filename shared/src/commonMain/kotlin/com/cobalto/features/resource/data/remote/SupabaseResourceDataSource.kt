package com.cobalto.features.resource.data.remote

import com.cobalto.core.common.domain.model.Resource
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

class SupabaseResourceDataSource(
    private val supabaseClient: io.github.jan.supabase.SupabaseClient
) : ResourceRemoteDataSource {

    override suspend fun getResources(): List<Resource> = withContext(Dispatchers.IO) {
        val allMaterials = mutableListOf<Resource>()
        var from = 0L
        val pageSize = 1000L
        while (true) {
            val page = supabaseClient.postgrest["Resource"]
                .select {
                    range(from, from + pageSize - 1)
                }
                .decodeList<Resource>()
            allMaterials.addAll(page)
            if (page.size < pageSize) break
            from += pageSize
        }
        allMaterials
    }

    override suspend fun updateResource(resource: Resource): Unit = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["Resource"]
            .upsert(resource)
        Unit
    }

    override suspend fun deleteResource(resourceId: String): Unit = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["Resource"]
            .delete {
                filter {
                    eq("resourceId", resourceId)
                }
            }
        Unit
    }

    override fun observeResources(): Flow<Unit> = callbackFlow {
        // Wait for auth to be initialized to avoid invalid access token error
        supabaseClient.auth.sessionStatus.first { it !is SessionStatus.Initializing }

        // Explicitly connect to realtime to ensure the token is used correctly
        supabaseClient.realtime.connect()

        val channel = supabaseClient.realtime.channel("resource_changes_${Random.nextLong()}")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "Resource"
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
