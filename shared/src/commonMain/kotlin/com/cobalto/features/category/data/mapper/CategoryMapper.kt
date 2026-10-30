package com.cobalto.features.category.data.mapper

import com.cobalto.core.database.category.CategoryEntity
import com.cobalto.features.category.domain.model.Category

fun CategoryEntity.toDomain(): Category {
    return Category(
        categoryId = categoryId,
        name = name,
        description = description,
        imagePath = imagePath
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        categoryId = categoryId,
        name = name,
        description = description,
        imagePath = imagePath
    )
}
