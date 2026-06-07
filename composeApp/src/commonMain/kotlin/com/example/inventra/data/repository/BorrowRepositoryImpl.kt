package com.example.inventra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.inventra.core.network.SupabaseClientProvider
import com.example.inventra.data.local.InventRaDatabase
import com.example.inventra.data.local.entity.toDomain
import com.example.inventra.data.remote.dto.BorrowRecordDto
import com.example.inventra.data.remote.dto.InsertBorrowDto
import com.example.inventra.data.remote.dto.ItemDto
import com.example.inventra.domain.model.BorrowRecord
import com.example.inventra.domain.model.BorrowStatus
import com.example.inventra.domain.repository.BorrowRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class BorrowRepositoryImpl(
    private val database: InventRaDatabase
) : BorrowRepository {

    private val client = SupabaseClientProvider.client
    private val db = client.postgrest
    private val auth = client.auth
    private val queries = database.borrowRecordQueries
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var lastSyncTime = 0L
    private val SYNC_COOLDOWN_MS = 60_000L

    override fun getAllRecords(): Flow<List<BorrowRecord>> {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastSyncTime > SYNC_COOLDOWN_MS) {
            syncScope.launch {
                syncRecordsFromSupabase()
                lastSyncTime = Clock.System.now().toEpochMilliseconds()
            }
        }
        return queries.getAllRecords()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }
    }

    override fun getActiveRecords(): Flow<List<BorrowRecord>> {
        return queries.getActiveRecords()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }
    }

    override suspend fun borrowItem(record: BorrowRecord): Long {
        println("DEBUG BorrowRepository.borrowItem: ${record.itemName}, itemId=${record.itemId}")

        // 1. Insert borrow record lokal
        queries.insertRecord(
            item_id = record.itemId,
            item_name = record.itemName,
            borrower_name = record.borrowerName,
            borrow_date = record.borrowDate.toEpochMilliseconds(),
            due_date = record.dueDate.toEpochMilliseconds(),
            return_date = null,
            status = BorrowStatus.PENDING.name,
            fine_amount = 0L
        )
        val localId = queries.lastInsertId().executeAsOne()

        // 2. Kurangi available_stock SEGERA
        val now = Clock.System.now().toEpochMilliseconds()
        try {
            val existingItem = database.itemQueries.getItemById(record.itemId).executeAsOneOrNull()
            if (existingItem != null && existingItem.available_stock > 0) {
                val newStock = existingItem.available_stock - 1
                database.itemQueries.updateAvailableStock(
                    available_stock = newStock,
                    updated_at = now,
                    id = record.itemId
                )
                println("BORROW: stok lokal ${existingItem.available_stock} → $newStock")
            }
        } catch (e: Exception) {
            println("BORROW: update stok lokal gagal: ${e.message}")
        }

        // 3. Reset cooldown agar sync tidak menimpa stok yang baru dikurangi
        lastSyncTime = now + SYNC_COOLDOWN_MS

        // 4. Sync ke Supabase di background
        syncScope.launch {
            try {
                val userId = auth.currentUserOrNull()?.id ?: "anonymous"
                val dto = InsertBorrowDto(
                    itemId = record.itemId.toString(),
                    borrowerId = userId,
                    itemName = record.itemName,
                    borrowerName = record.borrowerName,
                    division = "HMIF",
                    quantity = 1,
                    dueDate = record.dueDate.toString(),
                    status = BorrowStatus.PENDING.name
                )
                db["borrow_records"].insert(dto)

                // Update available_stock di Supabase
                val currentRemote = db["items"]
                    .select { filter { eq("id", record.itemId.toString()) } }
                    .decodeSingleOrNull<ItemDto>()
                if (currentRemote != null && currentRemote.availableStock > 0) {
                    db["items"].update(
                        mapOf("available_stock" to currentRemote.availableStock - 1)
                    ) { filter { eq("id", record.itemId.toString()) } }
                }
            } catch (e: Exception) {
                println("BORROW Supabase sync gagal: ${e.message}")
            }
        }

        return localId
    }

    override suspend fun returnItem(recordId: Long) {
        val returnDate = Clock.System.now()
        val record = queries.getAllRecords().executeAsList()
            .find { it.id == recordId } ?: return

        val dueDate = Instant.fromEpochMilliseconds(record.due_date)
        val fineAmount = if (returnDate > dueDate) {
            val diffMs = returnDate.toEpochMilliseconds() - dueDate.toEpochMilliseconds()
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
            diffDays * 10_000L
        } else 0L

        queries.updateRecordStatus(
            status = BorrowStatus.RETURNED.name,
            return_date = returnDate.toEpochMilliseconds(),
            fine_amount = fineAmount,
            id = recordId
        )

        // Kembalikan stok lokal
        try {
            val existingItem = database.itemQueries.getItemById(record.item_id).executeAsOneOrNull()
            if (existingItem != null) {
                val newStock = (existingItem.available_stock + 1).coerceAtMost(existingItem.total_stock)
                database.itemQueries.updateAvailableStock(
                    available_stock = newStock,
                    updated_at = Clock.System.now().toEpochMilliseconds(),
                    id = record.item_id
                )
            }
        } catch (e: Exception) {
            println("RETURN: update stok gagal: ${e.message}")
        }

        // Reset cooldown
        lastSyncTime = Clock.System.now().toEpochMilliseconds() + SYNC_COOLDOWN_MS

        syncScope.launch {
            try {
                db["borrow_records"].update(
                    mapOf(
                        "status" to BorrowStatus.RETURNED.name,
                        "fine_amount" to fineAmount,
                        "return_date" to returnDate.toString()
                    )
                ) { filter { eq("id", recordId.toString()) } }
            } catch (e: Exception) {
                println("RETURN Supabase sync gagal: ${e.message}")
            }
        }
    }

    override suspend fun approveRequest(recordId: Long) {
        queries.updateStatus(status = BorrowStatus.ACTIVE.name, id = recordId)
        syncScope.launch {
            try {
                db["borrow_records"].update(
                    mapOf("status" to BorrowStatus.ACTIVE.name)
                ) { filter { eq("id", recordId.toString()) } }
            } catch (e: Exception) {
                println("APPROVE Supabase sync gagal: ${e.message}")
            }
        }
    }

    override suspend fun refresh() {
        syncRecordsFromSupabase()
        lastSyncTime = Clock.System.now().toEpochMilliseconds()
    }

    override suspend fun deleteAll() {
        queries.deleteAll()
        try {
            // Hapus di Supabase (untuk demo reset)
            db["borrow_records"].delete {
                filter { neq("id", "0") } // Trick agar menghapus semua
            }
        } catch (e: Exception) {
            println("DELETE ALL Supabase gagal: ${e.message}")
        }
    }

    private suspend fun syncRecordsFromSupabase() {
        try {
            val records = db["borrow_records"]
                .select { order("created_at", Order.DESCENDING) }
                .decodeList<BorrowRecordDto>()

            database.transaction {
                queries.deleteAll()
                records.forEach { dto ->
                    queries.insertRecord(
                        item_id = 0L,
                        item_name = dto.itemName,
                        borrower_name = dto.borrowerName,
                        borrow_date = runCatching {
                            Instant.parse(dto.borrowDate).toEpochMilliseconds()
                        }.getOrDefault(Clock.System.now().toEpochMilliseconds()),
                        due_date = runCatching {
                            Instant.parse(dto.dueDate).toEpochMilliseconds()
                        }.getOrDefault(Clock.System.now().toEpochMilliseconds()),
                        return_date = dto.returnDate?.let {
                            runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull()
                        },
                        status = dto.status,
                        fine_amount = dto.fineAmount
                    )
                }
            }
            println("SYNC BORROW: ${records.size} records")
        } catch (e: Exception) {
            println("SYNC BORROW offline: ${e.message}")
        }
    }
}