package com.example.inventra.presentation.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inventra.domain.model.BorrowRecord
import com.example.inventra.domain.model.BorrowStatus
import com.example.inventra.presentation.components.EmptyState
import com.example.inventra.presentation.components.InventRaBottomNav
import com.example.inventra.presentation.components.LoadingIndicator
import com.example.inventra.presentation.util.formatDateOnly
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val viewModel: HistoryViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Aktif", "Semua")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Borrowing History",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        bottomBar = {
            InventRaBottomNav(currentRoute = currentRoute, onNavigate = onNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.secondary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Medium) }
                    )
                }
            }

            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    LoadingIndicator()
                }

                is HistoryUiState.Empty -> {
                    EmptyState(
                        title = "Belum Ada Riwayat",
                        description = "Belum ada transaksi peminjaman yang tercatat."
                    )
                }

                is HistoryUiState.Success -> {
                    // Filter berdasarkan tab yang dipilih
                    val displayedRecords = when (selectedTab) {
                        0 -> state.records.filter {
                            it.status == BorrowStatus.ACTIVE || it.status == BorrowStatus.OVERDUE
                        }
                        else -> state.records
                    }

                    // Summary overdue jika ada
                    val overdueCount = state.records.count { it.status == BorrowStatus.OVERDUE }
                    if (overdueCount > 0) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "$overdueCount item overdue!",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        "Segera tindak lanjuti.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    if (displayedRecords.isEmpty()) {
                        EmptyState(
                            title = "Tidak Ada Data",
                            description = if (selectedTab == 0)
                                "Tidak ada peminjaman aktif saat ini."
                            else
                                "Belum ada riwayat peminjaman."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayedRecords) { record ->
                                BorrowRecordCard(record = record)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BorrowRecordCard(record: BorrowRecord) {
    val isOverdue = record.status == BorrowStatus.OVERDUE
    val isReturned = record.status == BorrowStatus.RETURNED

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                isReturned -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Icon status
            Icon(
                imageVector = when {
                    isReturned -> Icons.Default.CheckCircle
                    isOverdue -> Icons.Default.Warning
                    else -> Icons.Default.History
                },
                contentDescription = null,
                tint = when {
                    isReturned -> MaterialTheme.colorScheme.secondary
                    isOverdue -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(20.dp).padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.itemName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Peminjam: ${record.borrowerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Dipinjam: ${record.borrowDate.formatDateOnly()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Jatuh tempo: ${record.dueDate.formatDateOnly()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline
                )
                if (isReturned && record.returnDate != null) {
                    Text(
                        text = "Dikembalikan: ${record.returnDate.formatDateOnly()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (record.fineAmount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Denda: Rp ${record.fineAmount.toLocaleString()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Status badge
            Surface(
                color = when {
                    isReturned -> MaterialTheme.colorScheme.secondaryContainer
                    isOverdue -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = when (record.status) {
                        BorrowStatus.RETURNED -> "SELESAI"
                        BorrowStatus.OVERDUE -> "OVERDUE"
                        BorrowStatus.ACTIVE -> "AKTIF"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isReturned -> MaterialTheme.colorScheme.onSecondaryContainer
                        isOverdue -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun Long.toLocaleString(): String {
    return this.toString().reversed().chunked(3).joinToString(".").reversed()
}