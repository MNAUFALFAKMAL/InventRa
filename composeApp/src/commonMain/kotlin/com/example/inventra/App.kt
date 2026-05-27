package com.example.inventra

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.inventra.presentation.navigation.AppNavHost
import com.example.inventra.presentation.theme.InventRaTheme
import com.example.inventra.presentation.theme.LocalThemeIsDark

@Composable
fun App() {
    // Cek apakah sistem device/HP sedang menggunakan mode gelap
    val isSystemDark = isSystemInDarkTheme()

    // Buat state yang bisa diubah (mutable) dan diinisialisasi dengan tema sistem
    val isDarkThemeState = remember { mutableStateOf(isSystemDark) }

    // Membungkus seluruh aplikasi dengan penyedia state Tema
    CompositionLocalProvider(LocalThemeIsDark provides isDarkThemeState) {

        // Memanggil tema dengan value dari state yang sudah disediakan di atas
        InventRaTheme(darkTheme = isDarkThemeState.value) {

            // Surface utama untuk memastikan warna background bereaksi terhadap perubahan tema
            Surface(
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                // Menjalankan Navigasi Aplikasi
                AppNavHost()
            }

        }
    }
}