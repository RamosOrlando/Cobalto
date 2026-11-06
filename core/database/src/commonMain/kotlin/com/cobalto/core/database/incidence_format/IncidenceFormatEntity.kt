package com.cobalto.core.database.incidence_format

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "IncidenceFormat",
    indices = [
        Index(value = ["codigo"], unique = true),
        Index(value = ["name"], unique = true),
        Index(value = ["decimalId"])
    ]
)
data class IncidenceFormatEntity(
    @PrimaryKey
    @ColumnInfo(name = "incidenceId")
    val incidenceId: String,

    @ColumnInfo(name = "codigo")
    val codigo: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "percent")
    val percent: Double,

    @ColumnInfo(name = "formula")
    val formula: String,

    @ColumnInfo(name = "decimalId")
    val decimalId: String,

    @ColumnInfo(name = "isVisible")
    val isVisible: Boolean
)
