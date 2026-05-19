package com.example.inventra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.inventra.data.local.NoteDatabase
import com.example.inventra.data.local.entity.toDomain
import com.example.inventra.data.local.entity.toDomainList
import com.example.inventra.domain.model.Item
import com.example.inventra.domain.repository.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ItemRepositoryImpl(private val database: NoteDatabase) : ItemRepository {

    private val queries = database.itemQueries

    override fun getItems(): Flow<List<Item>> {
        return queries.getAllItems()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toDomainList() }
    }

    override suspend fun getItemById(id: String): Item? = withContext(Dispatchers.IO) {
        queries.getItemById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insertItem(item: Item) = withContext(Dispatchers.IO) {
        queries.insertItem(
            id = item.id,
            name = item.name,
            category = item.category,
            location = item.location,
            status = item.status,
            imageUrl = item.imageUrl
        )
    }

    override suspend fun deleteItem(id: String) = withContext(Dispatchers.IO) {
        queries.deleteItemById(id)
    }
}
