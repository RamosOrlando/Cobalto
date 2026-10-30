package com.cobalto.core.database.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserProfession")
data class UserProfessionEntity(
    @PrimaryKey val professionId: Int,
    val name: String
)
