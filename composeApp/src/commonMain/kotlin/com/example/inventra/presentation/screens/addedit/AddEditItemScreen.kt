package com.example.inventra.presentation.screens.addedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inventra.domain.model.ItemCategory
import com.example.inventra.domain.model.ItemCondition
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    itemId: Long? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val viewModel: AddEditItemViewModel = koinViewModel { parametersOf(itemId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate back setelah save sukses
    LaunchedEffect(uiState.isSaving) {
        // isSaving berubah false setelah sukses (error akan mengisi uiState.error)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (itemId != null) "Edit Barang" else "Tambah Barang",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveItem(onSuccess = onSaveSuccess) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Simpan")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Nama Barang
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nama Barang *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Deskripsi
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            // Lokasi
            OutlinedTextField(
                value = uiState.location,
                onValueChange = viewModel::onLocationChange,
                label = { Text("Lokasi Penyimpanan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // PIC
            OutlinedTextField(
                value = uiState.picName,
                onValueChange = viewModel::onPicNameChange,
                label = { Text("Penanggung Jawab (PIC)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Stok Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.totalStock,
                    onValueChange = viewModel::onTotalStockChange,
                    label = { Text("Total Stok") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.availableStock,
                    onValueChange = viewModel::onAvailableStockChange,
                    label = { Text("Stok Tersedia") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Dropdown Kategori
            CategoryDropdown(
                selected = uiState.category,
                onSelected = viewModel::onCategoryChange
            )

            // Dropdown Kondisi
            ConditionDropdown(
                selected = uiState.condition,
                onSelected = viewModel::onConditionChange
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: ItemCategory,
    onSelected: (ItemCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = ItemCategory.entries.filter { it != ItemCategory.ALL }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategori") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName) },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionDropdown(
    selected: ItemCondition,
    onSelected: (ItemCondition) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kondisi Barang") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ItemCondition.entries.forEach { condition ->
                DropdownMenuItem(
                    text = { Text(condition.displayName) },
                    onClick = {
                        onSelected(condition)
                        expanded = false
                    }
                )
            }
        }
    }
}