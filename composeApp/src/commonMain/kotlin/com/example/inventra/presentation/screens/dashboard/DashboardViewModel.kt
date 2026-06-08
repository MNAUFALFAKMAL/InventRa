package com.example.inventra.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventra.domain.model.BorrowRecord
import com.example.inventra.domain.model.BorrowStatus
import com.example.inventra.domain.model.User
import com.example.inventra.domain.repository.AuthRepository
import com.example.inventra.domain.repository.BorrowRepository
import com.example.inventra.domain.repository.ItemRepository
import kotlinx.coroutines.flow.*

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val totalItems: Int,
        val borrowedItems: Int,
        val overdueItems: Int,
        val activeBorrowings: List<BorrowRecord>
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(
    itemRepository: ItemRepository,
    borrowRepository: BorrowRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = flow {
        emit(authRepository.getCurrentUser())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<DashboardUiState> = combine(
        itemRepository.getAllItems(),
        borrowRepository.getActiveRecords()
    ) { items, activeRecords ->
        val borrowedCount = items.count { it.availableStock < it.totalStock }
        val overdueCount = activeRecords.count { it.status == BorrowStatus.OVERDUE }
        DashboardUiState.Success(
            totalItems = items.size,
            borrowedItems = borrowedCount,
            overdueItems = overdueCount,
            activeBorrowings = activeRecords.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    fun refresh() {
        // Flow otomatis reaktif, tidak perlu manual refresh
    }
}