package com.cobalto.core.database.project

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ProjectModule",
    indices = [Index(value = ["projectId"])]
)
data class ProjectModuleEntity(
    @PrimaryKey val moduleId: String,
    @ColumnInfo(name = "projectId") val projectId: String = "",
    val code: String,
    val title: String,
    val orderIndex: Int = 0
)
