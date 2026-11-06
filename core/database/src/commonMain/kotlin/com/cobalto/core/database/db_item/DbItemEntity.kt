package com.cobalto.core.database.db_item

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "DbItem",
    indices = [Index(value = ["dbId"])]
)
data class DbItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "itemId")
    val itemId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "unit")
    val unit: String,
    @ColumnInfo(name = "dbId")
    val dbId: String,
    @ColumnInfo(name = "Code")
    val code: String? = null,
    @ColumnInfo(name = "imagePath")
    val imagePath: String? = null,
    @ColumnInfo(name = "total")
    val total: Double? = null
)
