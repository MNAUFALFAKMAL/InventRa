package com.example.inventra

import com.example.inventra.domain.model.BorrowRecord
import com.example.inventra.domain.model.BorrowStatus
import com.example.inventra.domain.model.Item
import com.example.inventra.domain.model.ItemCategory
import com.example.inventra.domain.model.User
import com.example.inventra.domain.model.UserDivision
import com.example.inventra.domain.model.UserRole
import com.example.inventra.domain.repository.AuthRepository
import com.example.inventra.domain.repository.BorrowRepository
import com.example.inventra.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeAuthRepository : AuthRepository {
    private val _currentUserFlow = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUserFlow.asStateFlow()
    
    var loggedInUser: User? = null
        set(value) {
            field = value
            _currentUserFlow.value = value
        }

    override val isLoggedIn: Boolean
        get() = loggedInUser != null

    override suspend fun login(email: String, password: String): Result<User> {
        val user = User(
            id = "1",
            name = "Test User",
            email = email,
            role = if (email.contains("admin")) UserRole.ADMIN else UserRole.MEMBER,
            division = UserDivision.PUBDOK
        )
        loggedInUser = user
        return Result.success(user)
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        division: String,
        role: String
    ): Result<User> {
        return Result.success(User(id = "2", name = name, email = email, role = UserRole.valueOf(role), division = UserDivision.valueOf(division)))
    }

    override suspend fun logout(): Result<Unit> {
        loggedInUser = null
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): User? = loggedInUser

    override suspend fun updateProfile(name: String, phone: String?, avatarUrl: String?): Result<User> {
        val updated = loggedInUser?.copy(name = name, phone = phone, avatarUrl = avatarUrl)
        loggedInUser = updated
        return Result.success(updated!!)
    }

    override suspend fun updateAvatar(imageBytes: ByteArray, fileName: String): Result<String> {
        return Result.success("https://fake-avatar.com/$fileName")
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return Result.success(emptyList())
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateUserRole(userId: String, role: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateUserName(userId: String, name: String): Result<Unit> {
        return Result.success(Unit)
    }
}

class FakeItemRepository : ItemRepository {
    private val items = MutableStateFlow<List<Item>>(emptyList())
    private var nextId = 1L
    
    override fun getAllItems(): Flow<List<Item>> = items
    
    override fun getItemsByCategory(category: ItemCategory): Flow<List<Item>> {
        return items.map { list -> 
            if (category == ItemCategory.ALL) list 
            else list.filter { it.category == category } 
        }
    }
    
    override fun searchItems(query: String): Flow<List<Item>> {
        return items.map { list ->
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                (it.description ?: "").contains(query, ignoreCase = true)
            }
        }
    }
    
    override fun getItemById(id: Long): Flow<Item?> {
        return items.map { list -> list.find { it.id == id } }
    }
    
    override suspend fun insertItem(item: Item): Long {
        val id = nextId++
        val newItem = item.copy(id = id)
        items.update { it + newItem }
        return id
    }
    
    override suspend fun updateItem(item: Item) {
        items.update { list ->
            list.map { if (it.id == item.id) item else it }
        }
    }
    
    override suspend fun deleteItem(id: Long) {
        items.update { list -> list.filter { it.id != id } }
    }

    override suspend fun uploadItemImage(imageBytes: ByteArray, fileName: String): Result<String> {
        return Result.success("https://fake-url.com/$fileName")
    }

    override suspend fun refresh() { }

    override suspend fun deleteAll() {
        items.update { emptyList() }
    }

    override suspend fun resetAllStocks() {
        items.update { list ->
            list.map { it.copy(availableStock = it.totalStock) }
        }
    }
}

class FakeBorrowRepository : BorrowRepository {
    private val records = MutableStateFlow<List<BorrowRecord>>(emptyList())

    fun addRecord(record: BorrowRecord) {
        records.update { it + record }
    }

    override fun getAllRecords(): Flow<List<BorrowRecord>> = records

    override fun getActiveRecords(): Flow<List<BorrowRecord>> = records.map { list ->
        list.filter { it.status == BorrowStatus.ACTIVE || it.status == BorrowStatus.OVERDUE }
    }

    override suspend fun borrowItem(record: BorrowRecord): Long {
        records.update { it + record }
        return record.id
    }

    override suspend fun returnItem(recordId: Long) {
        records.update { list ->
            list.map { if (it.id == recordId) it.copy(status = BorrowStatus.RETURNED) else it }
        }
    }

    override suspend fun approveRequest(recordId: Long) {
        records.update { list ->
            list.map { if (it.id == recordId) it.copy(status = BorrowStatus.ACTIVE) else it }
        }
    }

    override suspend fun refresh() { }

    override suspend fun deleteAll() {
        records.update { emptyList() }
    }
}
