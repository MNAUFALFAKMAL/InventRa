package com.example.inventra.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray, String) -> Unit
): ImagePickerLauncher {
    return remember {
        object : ImagePickerLauncher {
            override fun pickImage() {
                println("ImagePicker: iOS pickImage not implemented")
            }
            override fun takePhoto() {
                println("ImagePicker: iOS takePhoto not implemented")
            }
        }
    }
}
