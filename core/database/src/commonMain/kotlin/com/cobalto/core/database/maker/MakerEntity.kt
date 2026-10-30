package com.cobalto.core.database.maker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Maker")
data class MakerEntity(
    @PrimaryKey val makerId: String,
    val name: String,
    val imagePath: String?
)
