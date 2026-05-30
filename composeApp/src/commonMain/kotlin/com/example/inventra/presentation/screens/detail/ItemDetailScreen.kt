package com.example.inventra.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventra.presentation.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.secondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
        },
        bottomBar = {
            // Action Buttons Fixed di Bawah
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hubungi PIC")
                    }
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pinjam")
                    }
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
                .padding(16.dp)
        ) {
            // Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(Color.LightGray, RoundedCornerShape(12.dp)) // Placeholder Image
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AVAILABLE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text("LOGISTIK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("SKU: HMIF-L-042", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }

            Text("Jetson Nano Developer Kit", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sekre HMIF - Cabinet B1", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Specs Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Spesifikasi Teknis", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SpecRow("GPU", "128-core Maxwell")
                    SpecRow("CPU", "Quad-core ARM A57")
                    SpecRow("Memory", "4 GB 64-bit LPDDR4")
                    SpecRow("Storage", "microSD", isLast = true)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Policy Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Policy, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kebijakan Peminjaman", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    PolicyItem(Icons.Default.CalendarToday, "Durasi Maksimal", "3 Hari Kalender")
                    Spacer(modifier = Modifier.height(12.dp))
                    PolicyItem(Icons.Default.Payments, "Denda Keterlambatan", "Rp 5.000 / Hari", isError = true)
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom bar
        }
    }
}

@Composable
fun SpecRow(label: String, value: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
    if (!isLast) {
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
fun PolicyItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String, isError: Boolean = false) {
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}