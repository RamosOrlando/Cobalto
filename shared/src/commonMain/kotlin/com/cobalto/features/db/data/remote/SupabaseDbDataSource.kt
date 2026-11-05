package com.cobalto.features.db.data.remote

import com.cobalto.features.db.domain.model.DbItem
import com.cobalto.features.db.domain.model.DbItemResource
import com.cobalto.features.db.domain.model.DbSubItem
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

class SupabaseDbDataSource(
    private val supabaseClient: io.github.jan.supabase.SupabaseClient
) : DbRemoteDataSource {

    override suspend fun getDbs(): List<DbItem> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["Db"]
            .select()
            .decodeList<DbItem>()
    }

    override suspend fun getDbItems(dbId: String): List<DbSubItem> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["DbItem"]
            .select {
                filter {
                    eq("dbId", dbId)
                }
            }
            .decodeList<DbSubItem>()
    }

    override suspend fun getDbItemResources(itemId: String): List<DbItemResource> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["DbItemResource"]
            .select {
                filter {
                    eq("itemId", itemId)
                }
            }
            .decodeList<DbItemResource>()
    }

    override suspend fun insertDbItemResource(itemResource: DbItemResource): Unit = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["DbItemResource"]
            .upsert(itemResource)
        Unit
    }

    override suspend fun deleteDbItemResource(itemId: String, resourceId: String): Unit = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["DbItemResource"]
            .delete {
                filter {
                    eq("itemId", itemId)
                    eq("resourceId", resourceId)
                }
            }
        Unit
    }

    override suspend fun insertDbItem(dbItem: DbSubItem): Unit = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["DbItem"]
            .upsert(dbItem)
        Unit
    }

    override fun observeDbs(): Flow<Unit> = callbackFlow {
        supabaseClient.auth.sessionStatus.first { it !is SessionStatus.Initializing }
        supabaseClient.realtime.connect()

        val channel = supabaseClient.realtime.channel("db_changes_${Random.nextLong()}")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "Db"
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
