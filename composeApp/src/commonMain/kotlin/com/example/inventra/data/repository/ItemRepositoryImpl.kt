package com.example.inventra.data.repository

import com.example.inventra.core.network.SupabaseClientProvider
import com.example.inventra.data.remote.dto.InsertItemDto
import com.example.inventra.data.remote.dto.ItemDto
import com.example.inventra.domain.model.Item
import com.example.inventra.domain.model.ItemCategory
import com.example.inventra.domain.repository.ItemRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Clock

class ItemRepositoryImpl : ItemRepository {

    private val db = SupabaseClientProvider.client.postgrest

    override fun getAllItems(): Flow<List<Item>> = flow {
        try {
            val items = db["items"]
                .select {
                    filter { eq("is_active", true) }
                    order("updated_at", Order.DESCENDING)
                }
                .decodeList<ItemDto>()
                .map { it.toDomainModel() }
            println("ITEMS LOADED: ${items.size}")
            emit(items)
        } catch (e: Exception) {
            println("GET ALL ITEMS ERROR: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override fun getItemsByCategory(category: ItemCategory): Flow<List<Item>> = flow {
        try {
            val items = db["items"]
                .select {
                    filter {
                        eq("is_active", true)
                        if (category != ItemCategory.ALL) {
                            eq("category", category.name)
                        }
                    }
                    order("updated_at", Order.DESCENDING)
                }
                .decodeList<ItemDto>()
                .map { it.toDomainModel() }
            emit(items)
        } catch (e: Exception) {
            println("GET ITEMS ERROR: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override fun searchItems(query: String): Flow<List<Item>> = flow {
        try {
            val items = db["items"]
                .select {
                    filter {
                        or {
                            ilike("name", "%$query%")
                            ilike("description", "%$query%")
                        }
                        eq("is_active", true)
                    }
                }
                .decodeList<ItemDto>()
                .map { it.toDomainModel() }
            emit(items)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override fun getItemById(id: Long): Flow<Item?> = flow {
        emit(null)
    }

    override suspend fun insertItem(item: Item): Long {
        val dto = InsertItemDto(
            name = item.name,
            description = item.description,
            category = item.category.name,
            location = item.location,
            totalStock = item.totalStock,
            availableStock = item.availableStock,
            condition = item.condition.name,
            picName = item.picName,
            imageUrl = item.imageUrl
        )
        db["items"].insert(dto)
        return 0L
    }

    override suspend fun updateItem(item: Item) {
        db["items"].update(
            mapOf(
                "name" to item.name,
                "description" to item.description,
                "category" to item.category.name,
                "location" to item.location,
                "total_stock" to item.totalStock,
                "available_stock" to item.availableStock,
                "condition" to item.condition.name,
                "pic_name" to item.picName
            )
        ) {
            filter { eq("name", item.name) }
        }
    }

    override suspend fun deleteItem(id: Long) {}
}

fun ItemDto.toDomainModel(): Item {
    return Item(
        id = 0L,
        name = name,
        description = description,
        category = try {
            ItemCategory.valueOf(category)
        } catch (e: Exception) {
            ItemCategory.OTHER
        },
        location = location,
        totalStock = totalStock,
        availableStock = availableStock,
        condition = try {
            com.example.inventra.domain.model.ItemCondition.valueOf(condition)
        } catch (e: Exception) {
            com.example.inventra.domain.model.ItemCondition.GOOD
        },
        picName = picName,
        imageUrl = imageUrl
    )
}