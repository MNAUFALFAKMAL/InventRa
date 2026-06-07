package com.example.inventra.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray, String) -> Unit
): ImagePickerLauncher {
    // iOS: placeholder — implementasi UIImagePickerController membutuhkan
    // UIViewController yang belum tersedia langsung dari Compose Multiplatform.
    // Untuk saat ini mengembalikan launcher kosong.
    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                println("ImagePicker: iOS not implemented yet")
            }
        }
    }
}