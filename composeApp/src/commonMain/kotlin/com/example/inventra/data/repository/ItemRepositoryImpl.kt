package com.example.inventra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.inventra.core.network.SupabaseClientProvider
import com.example.inventra.data.local.InventRaDatabase
import com.example.inventra.data.local.entity.toDomain
import com.example.inventra.data.remote.dto.InsertItemDto
import com.example.inventra.data.remote.dto.ItemDto
import com.example.inventra.domain.model.Item
import com.example.inventra.domain.model.ItemCategory
import com.example.inventra.domain.repository.ItemRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ItemRepositoryImpl(
    private val database: InventRaDatabase
) : ItemRepository {

    private val db = SupabaseClientProvider.client.postgrest
    private val storageClient = SupabaseClientProvider.client.storage
    private val queries = database.itemQueries
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var lastSyncTime = 0L
    private val SYNC_COOLDOWN_MS = 60_000L

    override fun getAllItems(): Flow<List<Item>> {
        triggerSyncIfStale()
        return queries.getAllItems()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }
    }

    override fun getItemsByCategory(category: ItemCategory): Flow<List<Item>> {
        triggerSyncIfStale()
        return if (category == ItemCategory.ALL) {
            queries.getAllItems().asFlow().mapToList(Dispatchers.IO)
                .map { it.map { e -> e.toDomain() } }.catch { emit(emptyList()) }
        } else {
            queries.getItemsByCategory(category.name).asFlow().mapToList(Dispatchers.IO)
                .map { it.map { e -> e.toDomain() } }.catch { emit(emptyList()) }
        }
    }

    override fun searchItems(query: String): Flow<List<Item>> =
        queries.searchItems(query, query).asFlow().mapToList(Dispatchers.IO)
            .map { it.map { e -> e.toDomain() } }.catch { emit(emptyList()) }

    override fun getItemById(id: Long): Flow<Item?> =
        queries.getItemById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }.catch { emit(null) }

    override suspend fun insertItem(item: Item): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insertItem(
            name = item.name, description = item.description,
            category = item.category.name, location = item.location,
            total_stock = item.totalStock.toLong(),
            available_stock = item.availableStock.toLong(),
            condition = item.condition.name, pic_name = item.picName,
            image_url = item.imageUrl, created_at = now, updated_at = now
        )
        val localId = queries.lastInsertId().executeAsOne()
        syncScope.launch {
            try {
                db["items"].insert(InsertItemDto(
                    name = item.name, description = item.description,
                    category = item.category.name, location = item.location,
                    totalStock = item.totalStock, availableStock = item.availableStock,
                    condition = item.condition.name, picName = item.picName,
                    imageUrl = item.imageUrl
                ))
            } catch (e: Exception) {
                println("Supabase insert gagal: ${e.message}")
            }
        }
        return localId
    }

    override suspend fun updateItem(item: Item) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.updateItem(
            name = item.name, description = item.description,
            category = item.category.name, location = item.location,
            total_stock = item.totalStock.toLong(),
            available_stock = item.availableStock.toLong(),
            condition = item.condition.name, pic_name = item.picName,
            image_url = item.imageUrl, updated_at = now, id = item.id
        )
        syncScope.launch {
            try {
                val updateData = buildJsonObject {
                    put("name", item.name)
                    put("description", item.description)
                    put("category", item.category.name)
                    put("location", item.location)
                    put("total_stock", item.totalStock)
                    put("available_stock", item.availableStock)
                    put("condition", item.condition.name)
                    put("pic_name", item.picName)
                    if (item.imageUrl != null) put("image_url", item.imageUrl)
                }
                db["items"].update(updateData) { filter { eq("id", item.id.toString()) } }
            } catch (e: Exception) {
                println("Supabase update gagal: ${e.message}")
            }
        }
    }

    override suspend fun deleteItem(id: Long) {
        queries.deleteItem(id)
        syncScope.launch {
            try {
                db["items"].delete { filter { eq("id", id.toString()) } }
            } catch (e: Exception) {
                println("Supabase delete gagal: ${e.message}")
            }
        }
    }

    override suspend fun uploadItemImage(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = storageClient["items"]
            val path = "items/$fileName"
            bucket.upload(path, imageBytes) { upsert = true }
            Result.success(bucket.publicUrl(path))
        } catch (e: Exception) {
            Result.failure(Exception("Gagal upload foto: ${e.message}"))
        }
    }

    override suspend fun refresh() {
        syncItemsFromSupabase()
        lastSyncTime = Clock.System.now().toEpochMilliseconds()
    }

    override suspend fun deleteAll() {
        queries.deleteAll()
        try {
            db["items"].delete()
        } catch (e: Exception) {
            println("Supabase deleteAll gagal: ${e.message}")
        }
    }

    private fun triggerSyncIfStale() {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastSyncTime > SYNC_COOLDOWN_MS) {
            syncScope.launch {
                syncItemsFromSupabase()
                lastSyncTime = Clock.System.now().toEpochMilliseconds()
            }
        }
    }

    private suspend fun syncItemsFromSupabase() {
        try {
            val remoteItems = db["items"]
                .select { filter { eq("is_active", true) } }
                .decodeList<ItemDto>()
            database.transaction {
                queries.deleteAll()
                val now = Clock.System.now().toEpochMilliseconds()
                remoteItems.forEach { dto ->
                    queries.insertItem(
                        name = dto.name, description = dto.description,
                        category = dto.category, location = dto.location,
                        total_stock = dto.totalStock.toLong(),
                        available_stock = dto.availableStock.toLong(),
                        condition = dto.condition, pic_name = dto.picName,
                        image_url = dto.imageUrl,
                        created_at = now, updated_at = now
                    )
                }
            }
            println("SYNC ITEMS: ${remoteItems.size} items")
        } catch (e: Exception) {
            println("SYNC ITEMS offline: ${e.message}")
        }
    }
}