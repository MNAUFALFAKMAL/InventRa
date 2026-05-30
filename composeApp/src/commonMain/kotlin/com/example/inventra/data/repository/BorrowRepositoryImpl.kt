package com.example.inventra.data.repository

import com.example.inventra.core.network.SupabaseClientProvider
import com.example.inventra.data.local.InventRaDatabase
import com.example.inventra.data.local.entity.toDomain
import com.example.inventra.data.remote.dto.BorrowRecordDto
import com.example.inventra.data.remote.dto.InsertBorrowDto
import com.example.inventra.domain.model.BorrowRecord
import com.example.inventra.domain.model.BorrowStatus
import com.example.inventra.domain.repository.BorrowRepository
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
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
    private val finePerDay = 10_000L
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllRecords(): Flow<List<BorrowRecord>> {
        syncScope.launch { syncRecordsFromSupabase() }
        return queries.getAllRecords()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }
    }

    override fun getActiveRecords(): Flow<List<BorrowRecord>> {
        syncScope.launch { syncRecordsFromSupabase() }
        return queries.getActiveRecords()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }
    }

    override suspend fun borrowItem(record: BorrowRecord): Long {
        queries.insertRecord(
            item_id = record.itemId,
            item_name = record.itemName,
            borrower_name = record.borrowerName,
            borrow_date = record.borrowDate.toEpochMilliseconds(),
            due_date = record.dueDate.toEpochMilliseconds(),
            return_date = null,
            status = BorrowStatus.ACTIVE.name,
            fine_amount = 0L
        )
        val localId = queries.lastInsertId().executeAsOne()

        syncScope.launch {
            try {
                val userId = auth.currentUserOrNull()?.id ?: return@launch
                val dto = InsertBorrowDto(
                    itemId = record.itemId.toString(),
                    borrowerId = userId,
                    itemName = record.itemName,
                    borrowerName = record.borrowerName,
                    division = "HMIF",
                    quantity = 1,
                    dueDate = record.dueDate.toString(),
                    status = "ACTIVE"
                )
                db["borrow_records"].insert(dto)
            } catch (e: Exception) {
                println("Borrow sync gagal (offline?): ${e.message}")
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
            diffDays * finePerDay
        } else 0L

        queries.updateRecordStatus(
            status = BorrowStatus.RETURNED.name,
            return_date = returnDate.toEpochMilliseconds(),
            fine_amount = fineAmount,
            id = recordId
        )
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
            println("SYNC: ${records.size} borrow records synced")
        } catch (e: Exception) {
            println("SYNC: Borrow offline, pakai cache — ${e.message}")
        }
    }
}