package com.cobalto.core.database.section

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cobalto.core.database.category.CategoryEntity

@Entity(
    tableName = "Section",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class SectionEntity(
    @PrimaryKey val sectionId: String,
    val name: String,
    val categoryId: String,
    val imagePath: String?
)
