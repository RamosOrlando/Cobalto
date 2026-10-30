package com.cobalto.features.section.data.mapper

import com.cobalto.core.database.section.SectionEntity
import com.cobalto.features.section.domain.model.Section

fun SectionEntity.toDomain() = Section(
    sectionId = sectionId,
    name = name,
    categoryId = categoryId,
    imagePath = imagePath
)

fun Section.toEntity() = SectionEntity(
    sectionId = sectionId,
    name = name,
    categoryId = categoryId,
    imagePath = imagePath
)
