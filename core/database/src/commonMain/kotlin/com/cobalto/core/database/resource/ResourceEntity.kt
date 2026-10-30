package com.cobalto.core.database.resource

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Resource",
    indices = [
        Index(value = ["makerId"]),
        Index(value = ["sectionId"])
    ]
)
data class ResourceEntity(
    @PrimaryKey
    @ColumnInfo(name = "resourceId")
    val resourceId: String,
    val name: String,
    val unit: String,
    val makerId: String,
    val sectionId: String,
    val specId: String?,
    val providerId: String?,
    val price: Double?,
    val quoteDate: String?
)
