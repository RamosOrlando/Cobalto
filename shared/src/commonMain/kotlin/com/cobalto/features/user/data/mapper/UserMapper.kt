package com.cobalto.features.user.data.mapper

import com.cobalto.core.database.user.*
import com.cobalto.features.user.domain.model.*

// User
fun UserEntity.toDomain() = User(
    userId = userId,
    name = name,
    lastName = lastName,
    email = email,
    roleId = roleId,
    professionId = professionId,
    createdAt = createdAt,
    cellphone = cellphone
)

fun User.toEntity() = UserEntity(
    userId = userId,
    name = name,
    lastName = lastName,
    email = email,
    roleId = roleId,
    professionId = professionId,
    createdAt = createdAt,
    cellphone = cellphone
)

// Roles
fun UserRoleEntity.toDomain() = UserRole(
    roleId = roleId,
    name = name
)

fun UserRole.toEntity() = UserRoleEntity(
    roleId = roleId ?: 0,
    name = name
)

// Professions
fun UserProfessionEntity.toDomain() = UserProfession(
    professionId = professionId,
    name = name
)

fun UserProfession.toEntity() = UserProfessionEntity(
    professionId = professionId ?: 0,
    name = name
)

// Plans
fun UserPlanEntity.toDomain() = UserPlan(
    planId = planId,
    name = name,
    price = price,
    discountPrice = discountPrice,
    discountStartDate = discountStartDate,
    discountEndDate = discountEndDate,
    durationDays = durationDays,
    isActive = isActive,
    createdAt = createdAt
)

fun UserPlan.toEntity() = UserPlanEntity(
    planId = planId ?: 0,
    name = name,
    price = price,
    discountPrice = discountPrice,
    discountStartDate = discountStartDate,
    discountEndDate = discountEndDate,
    durationDays = durationDays,
    isActive = isActive,
    createdAt = createdAt
)

// Subscription History
fun SubscriptionHistoryEntity.toDomain() = SubscriptionHistory(
    subHistoryId = subHistoryId,
    userId = userId,
    planId = planId,
    startDate = startDate,
    endDate = endDate,
    state = state,
    pricePaid = pricePaid,
    discountAmount = discountAmount
)

fun SubscriptionHistory.toEntity() = SubscriptionHistoryEntity(
    subHistoryId = subHistoryId,
    userId = userId,
    planId = planId,
    startDate = startDate,
    endDate = endDate,
    state = state,
    pricePaid = pricePaid,
    discountAmount = discountAmount
)
