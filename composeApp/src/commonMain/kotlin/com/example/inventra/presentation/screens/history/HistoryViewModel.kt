package com.example.inventra.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventra.domain.model.BorrowRecord
import com.example.inventra.domain.model.User
import com.example.inventra.domain.model.UserRole
import com.example.inventra.domain.repository.AuthRepository
import com.example.inventra.domain.repository.BorrowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val records: List<BorrowRecord>) : HistoryUiState
    data object Empty : HistoryUiState
}

class HistoryViewModel(
    private val borrowRepository: BorrowRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = flow {
        emit(authRepository.getCurrentUser())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<HistoryUiState> = combine(
        borrowRepository.getAllRecords(),
        currentUser
    ) { records, user ->
        if (user == null) return@combine HistoryUiState.Loading
        
        val filteredRecords = if (user.role == UserRole.ADMIN) {
            records
        } else {
            records.filter { it.borrowerId == user.id }
        }
        
        if (filteredRecords.isEmpty()) HistoryUiState.Empty
        else HistoryUiState.Success(filteredRecords)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState.Loading
    )

    fun approveRequest(recordId: Long) {
        viewModelScope.launch {
            borrowRepository.approveRequest(recordId)
        }
    }

    fun returnItem(recordId: Long) {
        viewModelScope.launch {
            borrowRepository.returnItem(recordId)
        }
    }
}