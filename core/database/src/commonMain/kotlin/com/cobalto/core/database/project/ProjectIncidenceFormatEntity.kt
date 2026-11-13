package com.cobalto.core.database.project

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "ProjectIncidenceFormat",
    primaryKeys = ["projectId", "incidenceId"],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["codigo"]),
        Index(value = ["name"])
    ]
)
data class ProjectIncidenceFormatEntity(
    @ColumnInfo(name = "incidenceId")
    val incidenceId: String,

    @ColumnInfo(name = "projectId")
    val projectId: String = "",

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
