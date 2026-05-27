package com.example.inventra.presentation.screens.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventra.presentation.components.CategoryChip
import com.example.inventra.presentation.components.InventRaBottomNav
import com.example.inventra.presentation.components.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddItem: () -> Unit
) {
    val categories = listOf("Semua", "Pubdok", "Konten", "Dekraf", "Medis", "Logistik")
    var selectedCategory by remember { mutableStateOf("Semua") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Barang", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddItem,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        // MEMASANG BOTTOM NAV
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
            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            // Grid Items
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(6) { index ->
                    ItemCard(
                        title = "Tensimeter",
                        category = "Medis",
                        description = "Alat ukur tekanan darah digital.",
                        stock = 5,
                        isAvailable = true,
                        onClick = { onNavigateToDetail("item_$index") },
                        onBorrowClick = { /* TODO: Borrow action */ }
                    )
                }
            }
        }
    }
}