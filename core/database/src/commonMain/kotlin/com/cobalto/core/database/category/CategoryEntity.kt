package com.cobalto.core.database.category

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Category")
data class CategoryEntity(
    @PrimaryKey val categoryId: String,
    val name: String,
    val description: String?,
    val imagePath: String?
)
