package com.example.inventra.data.repository

import com.example.inventra.core.network.SupabaseClientProvider
import com.example.inventra.data.remote.dto.BorrowRecordDto
import com.example.inventra.data.remote.dto.InsertBorrowDto
import com.example.inventra.data.remote.dto.ItemDto
import com.example.inventra.domain.model.BorrowRecord
import com.example.inventra.domain.model.BorrowStatus
import com.example.inventra.domain.repository.BorrowRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class BorrowRepositoryImpl : BorrowRepository {

    private val client = SupabaseClientProvider.client
    private val db = client.postgrest
    private val auth = client.auth
    private val finePerDay = 10_000L

    override fun getAllRecords(): Flow<List<BorrowRecord>> = flow {
        try {
            val records = db["borrow_records"]
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<BorrowRecordDto>()
                .map { it.toDomainModel() }
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override fun getActiveRecords(): Flow<List<BorrowRecord>> = flow {
        try {
            val records = db["borrow_records"]
                .select {
                    filter {
                        or {
                            eq("status", "ACTIVE")
                            eq("status", "OVERDUE")
                            eq("status", "PENDING")
                        }
                    }
                    order("due_date", Order.ASCENDING)
                }
                .decodeList<BorrowRecordDto>()
                .map { it.toDomainModel() }
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun borrowItem(record: BorrowRecord): Long {
        val userId = auth.currentUserOrNull()?.id
            ?: throw Exception("Tidak terautentikasi")

        val dto = InsertBorrowDto(
            itemId = record.itemId.toString(),
            borrowerId = userId,
            itemName = record.itemName,
            borrowerName = record.borrowerName,
            division = "PUBDOK",
            quantity = 1,
            dueDate = record.dueDate.toString(),
            status = "PENDING"
        )
        db["borrow_records"].insert(dto)
        return 0L
    }

    override suspend fun returnItem(recordId: Long) {}

    suspend fun returnItemBySupabaseId(supabaseRecordId: String): Result<Unit> {
        return try {
            val returnDate = Clock.System.now()
            val record = db["borrow_records"]
                .select { filter { eq("id", supabaseRecordId) } }
                .decodeSingle<BorrowRecordDto>()

            val dueDate = Instant.parse(record.dueDate)
            val fineAmount = if (returnDate > dueDate) {
                val diffMs = returnDate.toEpochMilliseconds() - dueDate.toEpochMilliseconds()
                val diffDays = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
                diffDays * finePerDay
            } else 0L

            db["borrow_records"].update(
                mapOf(
                    "status" to "RETURNED",
                    "return_date" to returnDate.toString(),
                    "fine_amount" to fineAmount
                )
            ) {
                filter { eq("id", supabaseRecordId) }
            }

            val item = db["items"]
                .select { filter { eq("id", record.itemId) } }
                .decodeSingle<ItemDto>()

            db["items"].update(
                mapOf("available_stock" to (item.availableStock + record.quantity))
            ) {
                filter { eq("id", record.itemId) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun BorrowRecordDto.toDomainModel(): BorrowRecord {
    return BorrowRecord(
        id = 0L,
        itemId = 0L,
        itemName = itemName,
        borrowerName = borrowerName,
        borrowDate = Instant.parse(borrowDate),
        dueDate = Instant.parse(dueDate),
        returnDate = returnDate?.let { Instant.parse(it) },
        status = try {
            BorrowStatus.valueOf(status)
        } catch (e: Exception) {
            BorrowStatus.ACTIVE
        },
        fineAmount = fineAmount
    )
}