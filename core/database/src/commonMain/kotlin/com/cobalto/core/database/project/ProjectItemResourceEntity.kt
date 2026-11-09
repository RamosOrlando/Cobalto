package com.cobalto.core.database.project

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "ProjectItemResource",
    primaryKeys = ["itemId", "resourceId"],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["resourceId"])
    ]
)
data class ProjectItemResourceEntity(
    @ColumnInfo(name = "itemId")
    val itemId: String,
    @ColumnInfo(name = "resourceId")
    val resourceId: String,
    @ColumnInfo(name = "quantity")
    val quantity: Double
)
