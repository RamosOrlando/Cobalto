package com.cobalto.core.database.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserRole")
data class UserRoleEntity(
    @PrimaryKey val roleId: Int,
    val name: String
)
