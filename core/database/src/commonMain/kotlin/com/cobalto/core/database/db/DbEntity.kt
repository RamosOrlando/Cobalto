package com.cobalto.core.database.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Db")
data class DbEntity(
    @PrimaryKey
    @ColumnInfo(name = "dbId")
    val dbId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "imagePath")
    val imagePath: String? = null
)
