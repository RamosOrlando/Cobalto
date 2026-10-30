package com.cobalto.features.user.domain.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.user.domain.model.*
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun refreshMetadata(): ResultState<Unit> // Roles, Professions, Plans
    
    fun getRolesFlow(): Flow<ResultState<List<UserRole>>>
    fun getProfessionsFlow(): Flow<ResultState<List<UserProfession>>>
    fun getActivePlansFlow(): Flow<ResultState<List<UserPlan>>>
    
    fun getUserFlow(userId: String): Flow<ResultState<User?>>
    suspend fun getUser(userId: String): ResultState<User?>
    suspend fun getUserByEmail(email: String): ResultState<User?>
    suspend fun saveUser(user: User): ResultState<Unit>
    
    suspend fun refreshSubscriptionHistory(userId: String): ResultState<Unit>
    fun getSubscriptionHistoryFlow(userId: String): Flow<ResultState<List<SubscriptionHistory>>>
    suspend fun saveSubscriptionHistory(history: SubscriptionHistory): ResultState<Unit>
}
