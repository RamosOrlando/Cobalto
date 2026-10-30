package com.cobalto.core.database.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // User
    @Query("SELECT * FROM User WHERE userId = :userId")
    fun getUser(userId: String): Flow<UserEntity?>

    @Upsert
    suspend fun insertUser(user: UserEntity)

    // Roles
    @Query("SELECT * FROM UserRole")
    fun getRoles(): Flow<List<UserRoleEntity>>

    @Upsert
    suspend fun insertRoles(roles: List<UserRoleEntity>)

    // Professions
    @Query("SELECT * FROM UserProfession")
    fun getProfessions(): Flow<List<UserProfessionEntity>>

    @Upsert
    suspend fun insertProfessions(professions: List<UserProfessionEntity>)

    // Plans
    @Query("SELECT * FROM UserPlan WHERE isActive = 1")
    fun getActivePlans(): Flow<List<UserPlanEntity>>

    @Upsert
    suspend fun insertPlans(plans: List<UserPlanEntity>)

    // Subscription History
    @Query("SELECT * FROM SubscriptionHistory WHERE userId = :userId")
    fun getSubscriptionHistory(userId: String): Flow<List<SubscriptionHistoryEntity>>

    @Upsert
    suspend fun insertSubscriptionHistory(history: SubscriptionHistoryEntity)
}
