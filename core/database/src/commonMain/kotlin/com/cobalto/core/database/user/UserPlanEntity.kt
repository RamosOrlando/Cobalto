package com.cobalto.core.database.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserPlan")
data class UserPlanEntity(
    @PrimaryKey val planId: Int,
    val name: String,
    val price: Float,
    val discountPrice: Float?,
    val discountStartDate: String?,
    val discountEndDate: String?,
    val durationDays: Int,
    val isActive: Boolean,
    val createdAt: String
)
