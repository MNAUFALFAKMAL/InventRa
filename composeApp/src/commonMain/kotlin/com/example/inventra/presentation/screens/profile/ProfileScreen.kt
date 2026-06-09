package com.example.inventra.presentation.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.inventra.core.util.rememberImagePickerLauncher
import com.example.inventra.domain.model.User
import com.example.inventra.domain.model.UserRole
import com.example.inventra.domain.repository.AuthRepository
import com.example.inventra.presentation.components.InventRaBottomNav
import com.example.inventra.presentation.theme.LocalThemeIsDark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ==================== UI STATE ====================

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isEditMode: Boolean = false,
    val editName: String = "",
    val editPhone: String = "",
    val divisionMembers: List<User> = emptyList(),
    val isLoadingMembers: Boolean = false
)

// ==================== VIEWMODEL ====================

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val itemRepository: com.example.inventra.domain.repository.ItemRepository,
    private val borrowRepository: com.example.inventra.domain.repository.BorrowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun resetAllData() {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                itemRepository.deleteAll()
                borrowRepository.deleteAll()
                _uiState.update { it.copy(isSaving = false, successMessage = "Data berhasil direset") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Gagal reset data: ${e.message}") }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }

    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update {
                it.copy(isLoading = false, user = user,
                    editName = user?.name ?: "", editPhone = user?.phone ?: "")
            }
            if (user != null) loadDivisionMembers(user)
        }
    }

    private fun loadDivisionMembers(currentUser: User) {
        _uiState.update { it.copy(isLoadingMembers = true) }
        viewModelScope.launch {
            authRepository.getAllUsers()
                .onSuccess { users ->
                    val members = users.filter { it.division == currentUser.division }
                    _uiState.update { it.copy(divisionMembers = members, isLoadingMembers = false) }
                }
                .onFailure { _uiState.update { it.copy(isLoadingMembers = false) } }
        }
    }

    fun enterEditMode() = _uiState.update { it.copy(isEditMode = true) }
    fun exitEditMode() = _uiState.update { it.copy(isEditMode = false) }
    fun onNameChange(v: String) = _uiState.update { it.copy(editName = v) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(editPhone = v) }

    fun uploadAvatar(bytes: ByteArray, fileName: String) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            authRepository.updateAvatar(bytes, fileName)
                .onSuccess { url ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            user = it.user?.copy(avatarUrl = url),
                            successMessage = "Foto profil diperbarui"
                        )
                    }
                    loadProfile() // Re-fetch to ensure sync
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            authRepository.updateProfile(
                name = state.editName,
                phone = state.editPhone.ifBlank { null },
                avatarUrl = null
            ).onSuccess { user ->
                _uiState.update { it.copy(isSaving = false, isEditMode = false, user = user, successMessage = "Profil diperbarui") }
                loadProfile() // Re-fetch
                loadDivisionMembers(user)
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }
}

// ==================== SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme = LocalThemeIsDark.current
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberImagePickerLauncher { bytes, fileName ->
        viewModel.uploadAvatar(bytes, fileName)
    }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    val user = uiState.user
    val isAdmin = user?.role == UserRole.ADMIN

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                actions = {
                    if (!uiState.isEditMode) {
                        IconButton(onClick = viewModel::enterEditMode) {
                            Icon(Icons.Default.Edit, "Edit Profil")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { InventRaBottomNav(currentRoute = currentRoute, onNavigate = onNavigate) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Avatar ──────────────────────────────────────────────────────
            Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.BottomEnd) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (user?.avatarUrl != null) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                (user?.name ?: "?").take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp).clickable { imagePicker.launch() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Default.CameraAlt, "Ganti Foto", tint = Color.White,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Text("Tap kamera untuk ganti foto", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))

            // ── Nama & Role ────────────────────────────────────────────────
            Text(user?.name ?: "Pengguna", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAdmin) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                        Text("ADMIN", style = MaterialTheme.typography.labelSmall, color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Text(user?.division?.displayName ?: "", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(24.dp))

            // ── Edit Mode ──────────────────────────────────────────────────
            if (uiState.isEditMode) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Edit Profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = uiState.editName, onValueChange = viewModel::onNameChange,
                            label = { Text("Nama Lengkap") }, singleLine = true,
                            shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = uiState.editPhone, onValueChange = viewModel::onPhoneChange,
                            label = { Text("Nomor HP") }, singleLine = true,
                            shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = viewModel::exitEditMode, modifier = Modifier.weight(1f)) { Text("Batal") }
                            Button(onClick = viewModel::saveProfile, modifier = Modifier.weight(1f),
                                enabled = !uiState.isSaving) {
                                if (uiState.isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                else Text("Simpan")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Division Info ──────────────────────────────────────────────
            DivisionInfoCard(
                divisionName = user?.division?.displayName ?: "",
                members = uiState.divisionMembers,
                isLoading = uiState.isLoadingMembers
            )
            Spacer(Modifier.height(16.dp))

            // ── Settings ──────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Pengaturan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isDarkTheme.value) Icons.Default.DarkMode else Icons.Default.LightMode,
                                null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Tema Gelap", style = MaterialTheme.typography.titleSmall)
                                Text(if (isDarkTheme.value) "Aktif" else "Nonaktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Switch(checked = isDarkTheme.value, onCheckedChange = { isDarkTheme.value = it })
                    }
                }
            }

            // ── Admin: Manajemen Akun ──────────────────────────────────────
            if (isAdmin) {
                var showResetDialog by remember { mutableStateOf(false) }

                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Reset Semua Data") },
                        text = { Text("Yakin hapus semua item & riwayat peminjaman? Tidak bisa dibatalkan.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showResetDialog = false
                                    viewModel.resetAllData()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text("Hapus Semua", fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) { Text("Batal") }
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToUserManagement() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ManageAccounts, null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Manajemen Akun", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Buat dan kelola akun anggota divisi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showResetDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, null,
                            tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Reset Semua Data", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Hapus seluruh item dan riwayat peminjaman",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    viewModel.logout { onLogoutClick() }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout Akun", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Division Info Card ────────────────────────────────────────────────────────

@Composable
private fun DivisionInfoCard(divisionName: String, members: List<User>, isLoading: Boolean) {
    if (divisionName.isBlank()) return
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Divisi $divisionName", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }
            } else if (members.isEmpty()) {
                Text("Belum ada anggota terdaftar.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            } else {
                val admins = members.filter { it.role == UserRole.ADMIN }
                val regular = members.filter { it.role != UserRole.ADMIN }
                admins.forEach { admin ->
                    Surface(color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Admin / Kepala", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text(admin.name, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (regular.isNotEmpty()) {
                    Text("Anggota (${regular.size})", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 4.dp))
                    regular.forEachIndexed { index, member ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(28.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(member.name, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (index < regular.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(start = 38.dp))
                        }
                    }
                }
            }
        }
    }
}