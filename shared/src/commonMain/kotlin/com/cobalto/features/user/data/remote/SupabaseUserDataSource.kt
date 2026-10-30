package com.cobalto.features.user.data.remote

import com.cobalto.features.user.domain.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SupabaseUserDataSource(
    private val supabaseClient: SupabaseClient
) : UserRemoteDataSource {

    override suspend fun getRoles(): List<UserRole> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["UserRole"]
                .select()
                .decodeList<UserRole>()
        } catch (e: Exception) {
            println("SupabaseUserDataSource: Error fetching UserRole: ${e.message}")
            throw e
        }
    }

    override suspend fun getProfessions(): List<UserProfession> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["UserProfession"]
                .select()
                .decodeList<UserProfession>()
        } catch (e: Exception) {
            println("SupabaseUserDataSource: Error fetching UserProfession: ${e.message}")
            throw e
        }
    }

    override suspend fun getActivePlans(): List<UserPlan> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["UserPlan"]
                .select {
                    filter {
                        eq("isActive", true)
                    }
                }
                .decodeList<UserPlan>()
        } catch (e: Exception) {
            println("SupabaseUserDataSource: Error fetching UserPlan: ${e.message}")
            throw e
        }
    }

    override suspend fun getUser(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["User"]
                .select {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeSingleOrNull<User>()
        } catch (e: Exception) {
            println("SupabaseUserDataSource: Error fetching User by ID: ${e.message}")
            throw e
        }
    }

    override suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["User"]
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeSingleOrNull<User>()
        } catch (e: Exception) {
            println("SupabaseUserDataSource: Error fetching User by email: ${e.message}")
            throw e
        }
    }

    override suspend fun upsertUser(user: User): Unit = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["User"]
            .upsert(user)
    }

    override suspend fun getSubscriptionHistory(userId: String): List<SubscriptionHistory> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["SubscriptionHistory"]
                .select {
                    filter {
                        eq("userId", userId)
                    }
                }
                .decodeList<SubscriptionHistory>()
        } catch (e: Exception) {
            println("SupabaseUserDataSource: Error fetching SubscriptionHistory: ${e.message}")
            throw e
        }
    }

    override suspend fun upsertSubscriptionHistory(history: SubscriptionHistory): Unit = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["SubscriptionHistory"]
            .upsert(history)
    }
}
