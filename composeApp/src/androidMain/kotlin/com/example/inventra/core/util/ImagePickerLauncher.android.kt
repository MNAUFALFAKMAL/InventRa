package com.example.inventra.core.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
): ImagePickerLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { stream ->
                stream.readBytes()
            }
            if (bytes != null) {
                val fileName = "upload_${System.currentTimeMillis()}.jpg"
                onImagePicked(bytes, fileName)
            }
        }
    }

    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                launcher.launch("image/*")
            }
        }
    }
}