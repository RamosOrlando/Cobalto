package com.cobalto.features.user.data.repository

import com.cobalto.core.domain.util.ResultState
import com.cobalto.core.database.user.UserDao
import com.cobalto.features.user.data.mapper.toDomain
import com.cobalto.features.user.data.mapper.toEntity
import com.cobalto.features.user.data.remote.UserRemoteDataSource
import com.cobalto.features.user.domain.model.*
import com.cobalto.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {

    override suspend fun refreshMetadata(): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            println("UserRepository: Refreshing metadata (Roles, Professions, Plans)...")
            
            val roles = remoteDataSource.getRoles()
            println("UserRepository: Fetched ${roles.size} roles from Supabase")
            
            val professions = remoteDataSource.getProfessions()
            println("UserRepository: Fetched ${professions.size} professions from Supabase")
            
            val plans = remoteDataSource.getActivePlans()
            println("UserRepository: Fetched ${plans.size} plans from Supabase")
            
            userDao.insertRoles(roles.map { it.toEntity() })
            userDao.insertProfessions(professions.map { it.toEntity() })
            userDao.insertPlans(plans.map { it.toEntity() })
            
            println("UserRepository: Metadata successfully synchronized to Room")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            println("UserRepository: ERROR refreshing metadata: ${e.message}")
            e.printStackTrace()
            ResultState.Error(e.message ?: "Error refreshing metadata")
        }
    }

    override fun getRolesFlow(): Flow<ResultState<List<UserRole>>> =
        userDao.getRoles()
            .map { entities -> ResultState.Success(entities.map { it.toDomain() }) as ResultState<List<UserRole>> }
            .onStart { emit(ResultState.Loading) }
            .catch { emit(ResultState.Error(it.message ?: "Error loading roles")) }

    override fun getProfessionsFlow(): Flow<ResultState<List<UserProfession>>> =
        userDao.getProfessions()
            .map { entities -> ResultState.Success(entities.map { it.toDomain() }) as ResultState<List<UserProfession>> }
            .onStart { emit(ResultState.Loading) }
            .catch { emit(ResultState.Error(it.message ?: "Error loading professions")) }

    override fun getActivePlansFlow(): Flow<ResultState<List<UserPlan>>> =
        userDao.getActivePlans()
            .map { entities -> ResultState.Success(entities.map { it.toDomain() }) as ResultState<List<UserPlan>> }
            .onStart { emit(ResultState.Loading) }
            .catch { emit(ResultState.Error(it.message ?: "Error loading plans")) }

    override fun getUserFlow(userId: String): Flow<ResultState<User?>> =
        userDao.getUser(userId)
            .map { entity -> ResultState.Success(entity?.toDomain()) as ResultState<User?> }
            .onStart { emit(ResultState.Loading) }
            .catch { emit(ResultState.Error(it.message ?: "Error loading user")) }

    override suspend fun getUser(userId: String): ResultState<User?> = withContext(Dispatchers.IO) {
        try {
            val remoteUser = remoteDataSource.getUser(userId)
            remoteUser?.let { 
                userDao.insertUser(it.toEntity())
            }
            ResultState.Success(remoteUser)
        } catch (e: Exception) {
            // Fallback to local
            val localUser = userDao.getUser(userId).firstOrNull()?.toDomain()
            ResultState.Success(localUser)
        }
    }

    override suspend fun getUserByEmail(email: String): ResultState<User?> = withContext(Dispatchers.IO) {
        try {
            val remoteUser = remoteDataSource.getUserByEmail(email)
            ResultState.Success(remoteUser)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error checking user existence")
        }
    }

    override suspend fun saveUser(user: User): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.upsertUser(user)
            userDao.insertUser(user.toEntity())
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error saving user")
        }
    }

    override suspend fun refreshSubscriptionHistory(userId: String): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            val history = remoteDataSource.getSubscriptionHistory(userId)
            history.forEach { 
                userDao.insertSubscriptionHistory(it.toEntity())
            }
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error refreshing subscription history")
        }
    }

    override fun getSubscriptionHistoryFlow(userId: String): Flow<ResultState<List<SubscriptionHistory>>> =
        userDao.getSubscriptionHistory(userId)
            .map { entities -> ResultState.Success(entities.map { it.toDomain() }) as ResultState<List<SubscriptionHistory>> }
            .onStart { emit(ResultState.Loading) }
            .catch { emit(ResultState.Error(it.message ?: "Error loading subscription history")) }

    override suspend fun saveSubscriptionHistory(history: SubscriptionHistory): ResultState<Unit> = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.upsertSubscriptionHistory(history)
            userDao.insertSubscriptionHistory(history.toEntity())
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error saving subscription history")
        }
    }
}
