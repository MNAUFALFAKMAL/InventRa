package com.example.inventra.data.repository

import com.example.inventra.core.network.SupabaseClientProvider
import com.example.inventra.data.local.InventRaDatabase
import com.example.inventra.data.local.entity.toDomain
import com.example.inventra.data.remote.dto.InsertItemDto
import com.example.inventra.data.remote.dto.ItemDto
import com.example.inventra.domain.model.Item
import com.example.inventra.domain.model.ItemCategory
import com.example.inventra.domain.repository.ItemRepository
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock

class ItemRepositoryImpl(
    private val database: InventRaDatabase
) : ItemRepository {

    private val db = SupabaseClientProvider.client.postgrest
    private val queries = database.itemQueries
    // Scope khusus untuk background sync agar tidak cancel saat Flow selesai
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllItems(): Flow<List<Item>> {
        // 1. Langsung emit dari SQLDelight (offline-first)
        // 2. Background: fetch Supabase dan update cache
        syncScope.launch { syncItemsFromSupabase() }

        return queries.getAllItems()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }
    }

    override fun getItemsByCategory(category: ItemCategory): Flow<List<Item>> {
        syncScope.launch { syncItemsFromSupabase() }

        return if (category == ItemCategory.ALL) {
            queries.getAllItems()
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { entities -> entities.map { it.toDomain() } }
                .catch { emit(emptyList()) }
        } else {
            queries.getItemsByCategory(category.name)
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { entities -> entities.map { it.toDomain() } }
                .catch { emit(emptyList()) }
        }
    }

    override fun searchItems(query: String): Flow<List<Item>> {
        return queries.searchItems(query, query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }
    }

    override fun getItemById(id: Long): Flow<Item?> {
        return queries.getItemById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity -> entity?.toDomain() }
            .catch { emit(null) }
    }

    override suspend fun insertItem(item: Item): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        // Simpan ke SQLDelight dulu
        queries.insertItem(
            name = item.name,
            description = item.description,
            category = item.category.name,
            location = item.location,
            total_stock = item.totalStock.toLong(),
            available_stock = item.availableStock.toLong(),
            condition = item.condition.name,
            pic_name = item.picName,
            image_url = item.imageUrl,
            created_at = now,
            updated_at = now
        )
        val localId = queries.lastInsertId().executeAsOne()

        // Sync ke Supabase di background
        syncScope.launch {
            try {
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
            } catch (e: Exception) {
                println("Supabase sync gagal (offline?): ${e.message}")
            }
        }

        return localId
    }

    override suspend fun updateItem(item: Item) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.updateItem(
            name = item.name,
            description = item.description,
            category = item.category.name,
            location = item.location,
            total_stock = item.totalStock.toLong(),
            available_stock = item.availableStock.toLong(),
            condition = item.condition.name,
            pic_name = item.picName,
            image_url = item.imageUrl,
            updated_at = now,
            id = item.id
        )

        syncScope.launch {
            try {
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
                ) { filter { eq("name", item.name) } }
            } catch (e: Exception) {
                println("Supabase sync gagal (offline?): ${e.message}")
            }
        }
    }

    override suspend fun deleteItem(id: Long) {
        queries.deleteItem(id)
    }

    // Background sync: fetch Supabase → update SQLDelight cache
    private suspend fun syncItemsFromSupabase() {
        try {
            val remoteItems = db["items"]
                .select { filter { eq("is_active", true) } }
                .decodeList<ItemDto>()

            database.transaction {
                queries.deleteAll()
                remoteItems.forEach { dto ->
                    val now = Clock.System.now().toEpochMilliseconds()
                    queries.insertItem(
                        name = dto.name,
                        description = dto.description,
                        category = dto.category,
                        location = dto.location,
                        total_stock = dto.totalStock.toLong(),
                        available_stock = dto.availableStock.toLong(),
                        condition = dto.condition,
                        pic_name = dto.picName,
                        image_url = dto.imageUrl,
                        created_at = now,
                        updated_at = now
                    )
                }
            }
            println("SYNC: ${remoteItems.size} items synced from Supabase")
        } catch (e: Exception) {
            println("SYNC: Supabase tidak tersedia, pakai cache lokal — ${e.message}")
        }
    }
}