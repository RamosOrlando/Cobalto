package com.cobalto.core.database.price_history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cobalto.core.database.resource.ResourceEntity
import com.cobalto.core.database.provider.ProviderEntity

@Entity(
    tableName = "PriceHistory",
    indices = [
        Index(value = ["resourceId"]),
        Index(value = ["providerId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ResourceEntity::class,
            parentColumns = ["resourceId"],
            childColumns = ["resourceId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["providerId"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
data class PriceHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "historyId")
    val historyId: String,
    @ColumnInfo(name = "resourceId")
    val materialId: String,
    @ColumnInfo(name = "providerId")
    val providerId: String,
    @ColumnInfo(name = "price")
    val price: Double,
    @ColumnInfo(name = "quoteDate")
    val quoteDate: String,
    @ColumnInfo(name = "username")
    val username: String
)
