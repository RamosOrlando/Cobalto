package com.cobalto.core.database.provider

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Provider",
    indices = [
        Index(value = ["name", "address", "city"], unique = true)
    ]
)
data class ProviderEntity(
    @PrimaryKey val providerId: String,
    val name: String,
    val address: String?,
    val telephone: String?,
    val city: String,
    val email: String?,
    val imagePath: String?
)
