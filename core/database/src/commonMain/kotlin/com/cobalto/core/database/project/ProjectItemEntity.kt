package com.cobalto.core.database.project

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ProjectItem",
    indices = [Index(value = ["moduleId"])]
)
data class ProjectItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "itemId")
    val itemId: String,
    @ColumnInfo(name = "moduleId")
    val moduleId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "unit")
    val unit: String,
    @ColumnInfo(name = "dbId")
    val dbId: String? = null,
    @ColumnInfo(name = "Code")
    val code: String? = null,
    @ColumnInfo(name = "imagePath")
    val imagePath: String? = null,
    @ColumnInfo(name = "unitPrice")
    val unitPrice: Double = 0.0,
    @ColumnInfo(name = "quantity")
    val quantity: Double = 0.0,
    @ColumnInfo(name = "totalAmount")
    val totalAmount: Double = quantity * unitPrice,
    @ColumnInfo(name = "orderIndex")
    val orderIndex: Int = 0
)
