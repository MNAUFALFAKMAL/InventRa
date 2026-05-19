package com.example.inventra.data.local.entity

import com.example.inventra.data.local.ItemEntity
import com.example.inventra.domain.model.Item

fun ItemEntity.toDomain(): Item {
    return Item(
        id = id,
        name = name,
        category = category,
        location = location,
        status = status,
        imageUrl = imageUrl
    )
}

fun Item.toEntity(): ItemEntity {
    return ItemEntity(
        id = id,
        name = name,
        category = category,
        location = location,
        status = status,
        imageUrl = imageUrl
    )
}

fun List<ItemEntity>.toDomainList(): List<Item> {
    return map { it.toDomain() }
}
