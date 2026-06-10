package com.example.inventra.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
): ImagePickerLauncher {
    val context = LocalContext.current

    // Launcher for Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { stream ->
                stream.readBytes()
            }
            if (bytes != null) {
                val fileName = "gallery_${System.currentTimeMillis()}.jpg"
                onImagePicked(bytes, fileName)
            }
        }
    }

    // Camera setup
    val tempFile = remember {
        File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    }
    val authority = "${context.packageName}.fileprovider"
    val tempUri = remember {
        FileProvider.getUriForFile(context, authority, tempFile)
    }

    // Launcher for Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bytes = tempFile.readBytes()
            val fileName = "camera_${System.currentTimeMillis()}.jpg"
            onImagePicked(bytes, fileName)
        }
    }

    return remember {
        object : ImagePickerLauncher {
            override fun pickImage() {
                galleryLauncher.launch("image/*")
            }

            override fun takePhoto() {
                cameraLauncher.launch(tempUri)
            }
        }
    }
}
