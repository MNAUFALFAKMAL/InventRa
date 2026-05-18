package com.example.inventra.domain.repository

import com.example.inventra.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(): Flow<List<Item>>
    suspend fun getItemById(id: String): Item?
    suspend fun insertItem(item: Item)
    suspend fun deleteItem(id: String)
}
