package com.cobalto.core.database.project

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Project")
data class ProjectEntity(
    @PrimaryKey val projectId: String,
    val name: String,
    val description: String?,
    val filePath: String,
    val createdAt: String,
    val updatedAt: String,
    val totalAmount: Double = 0.0
)
