package com.cobalto.core.database.user

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SubscriptionHistory",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserPlanEntity::class,
            parentColumns = ["planId"],
            childColumns = ["planId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("userId"),
        Index("planId")
    ]
)
data class SubscriptionHistoryEntity(
    @PrimaryKey val subHistoryId: String,
    val userId: String,
    val planId: Int,
    val startDate: String,
    val endDate: String,
    val state: String,
    val pricePaid: Float,
    val discountAmount: Float
)
