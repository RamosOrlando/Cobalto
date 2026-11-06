package com.cobalto.core.database.decimal_format

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "DecimalFormat",
    indices = [
        Index(value = ["codigo"], unique = true),
        Index(value = ["name"], unique = true)
    ]
)
data class DecimalFormatEntity(
    @PrimaryKey
    @ColumnInfo(name = "decimalId")
    val decimalId: String,

    @ColumnInfo(name = "codigo")
    val codigo: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "number")
    val number: Int
)
