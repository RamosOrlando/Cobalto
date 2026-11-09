package com.cobalto.core.database.project

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProjectModule")
data class ProjectModuleEntity(
    @PrimaryKey val moduleId: String,
    val code: String,
    val title: String,
    val orderIndex: Int = 0
)
