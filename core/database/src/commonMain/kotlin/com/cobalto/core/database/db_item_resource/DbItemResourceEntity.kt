package com.cobalto.core.database.db_item_resource

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "DbItemResource",
    primaryKeys = ["itemId", "resourceId"],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["resourceId"])
    ]
)
data class DbItemResourceEntity(
    @ColumnInfo(name = "itemId")
    val itemId: String,
    @ColumnInfo(name = "resourceId")
    val resourceId: String,
    @ColumnInfo(name = "quantity")
    val quantity: Double
)
