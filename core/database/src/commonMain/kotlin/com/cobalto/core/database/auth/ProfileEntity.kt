package com.cobalto.core.database.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Profile")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val fullName: String?,
    val email: String,
    val role: String // Store as String for Room simplicity
)
