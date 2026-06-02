package com.example.inventra.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inventra.domain.model.UserDivision
import com.example.inventra.presentation.components.GlassCard
import com.example.inventra.presentation.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit = {}
) {
    val viewModel: ItemDetailViewModel = koinViewModel { parametersOf(itemId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Semua dialog state di level tertinggi composable
    var showBorrowDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var borrowerName by remember { mutableStateOf("") }
    var borrowerDivision by remember { mutableStateOf(UserDivision.PUBDOK) }
    var showDivisionDropdown by remember { mutableStateOf(false) }
    var borrowError by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

    // Resolve successState di luar Scaffold agar bisa diakses di bottomBar
    val successState = uiState as? ItemDetailUiState.Success

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(snackbarMessage)
            snackbarMessage = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detail Barang",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    if (successState != null) {
                        IconButton(onClick = { onNavigateToEdit(successState.item.id) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        bottomBar = {
            // Gunakan successState yang sudah di-resolve di atas
            if (successState != null) {
                val item = successState.item
                val uriHandler = LocalUriHandler.current
                val whatsappUrl = "https://wa.me/6287714891011?text=" +
                        "Halo Admin, saya ingin info peminjaman *${item.name}* " +
                        "dari inventaris HMIF ITERA."

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { uriHandler.openUri(whatsappUrl) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hubungi PIC")
                        }
                        Button(
                            onClick = {
                                borrowError = ""
                                showBorrowDialog = true
                            },
                            enabled = item.isBorrowable,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (item.isBorrowable) "Pinjam" else "Habis")
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is ItemDetailUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }

            is ItemDetailUiState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Barang tidak ditemukan",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            is ItemDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is ItemDetailUiState.Success -> {
                val item = state.item

                // Debug log
                println("DEBUG UI item: name=${item.name} available=${item.availableStock} total=${item.totalStock} isBorrowable=${item.isBorrowable}")

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Image placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )

                        // Status badge
                        Surface(
                            color = if (item.isBorrowable)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (item.isBorrowable) Icons.Default.CheckCircle
                                    else Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (item.isBorrowable)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    item.statusLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isBorrowable)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            item.category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        item.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (item.description.isNotBlank()) {
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            item.location.ifBlank { "Lokasi tidak diset" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Info Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Informasi Barang",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow("Kondisi", item.condition.displayName)
                            InfoRow("Total Stok", "${item.totalStock} unit")
                            InfoRow("Tersedia", "${item.availableStock} unit")
                            InfoRow("PIC", item.picName.ifBlank { "-" }, isLast = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Policy Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Policy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Kebijakan Peminjaman",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            PolicyItem(
                                Icons.Default.CalendarToday,
                                "Durasi Maksimal",
                                "2 Hari Kalender"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            PolicyItem(
                                Icons.Default.Payments,
                                "Denda Keterlambatan",
                                "Rp 10.000 / Hari",
                                isError = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            PolicyItem(
                                Icons.Default.Person,
                                "Admin",
                                "Nabila Ramadhani Mujahidin (Bendahara Umum)"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // ==================== BORROW DIALOG ====================
    // Di luar Scaffold agar state showBorrowDialog bisa diakses
    // dari bottomBar dan dialog ini tanpa masalah scope
    if (showBorrowDialog) {
        AlertDialog(
            onDismissRequest = {
                showBorrowDialog = false
                borrowError = ""
            },
            title = { Text("Ajukan Peminjaman", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Permintaan akan diproses oleh Admin (Nabila).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    OutlinedTextField(
                        value = borrowerName,
                        onValueChange = { borrowerName = it },
                        label = { Text("Nama Peminjam *") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = showDivisionDropdown,
                        onExpandedChange = { showDivisionDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = borrowerDivision.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Divisi") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = showDivisionDropdown
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = showDivisionDropdown,
                            onDismissRequest = { showDivisionDropdown = false }
                        ) {
                            UserDivision.entries.forEach { division ->
                                DropdownMenuItem(
                                    text = { Text(division.displayName) },
                                    onClick = {
                                        borrowerDivision = division
                                        showDivisionDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Tampilkan error jika ada
                    if (borrowError.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                borrowError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (borrowerName.isNotBlank()) {
                            borrowError = ""
                            viewModel.requestBorrow(
                                borrowerName = borrowerName,
                                borrowerDivision = borrowerDivision.displayName,
                                onSuccess = {
                                    showBorrowDialog = false
                                    borrowerName = ""
                                    borrowError = ""
                                    snackbarMessage =
                                        "✅ Permintaan peminjaman terkirim! Tunggu konfirmasi admin."
                                },
                                onError = { msg ->
                                    borrowError = msg
                                }
                            )
                        }
                    },
                    enabled = borrowerName.isNotBlank()
                ) {
                    Text("Ajukan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBorrowDialog = false
                    borrowError = ""
                }) {
                    Text("Batal")
                }
            }
        )
    }

    // ==================== DELETE DIALOG ====================
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Barang", fontWeight = FontWeight.Bold) },
            text = {
                Text("Yakin ingin menghapus barang ini? Tindakan tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteItem { onNavigateBack() }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ==================== PRIVATE COMPOSABLES ====================

@Composable
private fun InfoRow(label: String, value: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun PolicyItem(
    icon: ImageVector,
    title: String,
    desc: String,
    isError: Boolean = false
) {
    val color = if (isError) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.secondary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}