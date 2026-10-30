package com.cobalto.core.database.user

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "User",
    foreignKeys = [
        ForeignKey(
            entity = UserRoleEntity::class,
            parentColumns = ["roleId"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserProfessionEntity::class,
            parentColumns = ["professionId"],
            childColumns = ["professionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("roleId"),
        Index("professionId")
    ]
)
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val lastName: String,
    val email: String,
    val roleId: Int,
    val professionId: Int,
    val createdAt: String,
    val cellphone: Int?
)
