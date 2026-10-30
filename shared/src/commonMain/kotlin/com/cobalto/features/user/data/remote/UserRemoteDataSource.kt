package com.cobalto.features.user.data.remote

import com.cobalto.features.user.domain.model.*

interface UserRemoteDataSource {
    suspend fun getRoles(): List<UserRole>
    suspend fun getProfessions(): List<UserProfession>
    suspend fun getActivePlans(): List<UserPlan>
    
    suspend fun getUser(userId: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun upsertUser(user: User)
    
    suspend fun getSubscriptionHistory(userId: String): List<SubscriptionHistory>
    suspend fun upsertSubscriptionHistory(history: SubscriptionHistory)
}
