package com.example.inventra.presentation.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventra.presentation.components.InventRaBottomNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIInventoryScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HMIF Inventaris", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
                        Icon(Icons.Default.Psychology, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            )
        },
        // MEMASANG BOTTOM NAV
        bottomBar = {
            InventRaBottomNav(currentRoute = currentRoute, onNavigate = onNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("How can I assist your inventory today?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                // (Isi dengan balon chat AI seperti kode sebelumnya)
            }

            // Input Field
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)).padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about items...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                    IconButton(
                        onClick = { /* Send */ },
                        modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}