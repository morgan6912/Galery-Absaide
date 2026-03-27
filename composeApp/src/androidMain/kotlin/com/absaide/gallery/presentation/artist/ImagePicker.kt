package com.absaide.gallery.presentation.artist


import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest

// 1. La implementación REAL de la clase para Android
actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

// 2. Las implementaciones REALES de las funciones para Android
@Composable
actual fun rememberImagePicker(
    onImageSelected: (ByteArray, String) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver
                .openInputStream(it)?.readBytes() ?: return@let
            val name = it.lastPathSegment ?: "imagen.jpg"
            onImageSelected(bytes, name)
        }
    }
    // Retornamos nuestra clase actual que ejecuta el launcher de Android
    return ImagePickerLauncher { launcher.launch("image/*") }
}

@Composable
actual fun ImageFromBytes(bytes: ByteArray) {
    val bitmap = remember(bytes) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }
    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
actual fun NetworkImage(url: String) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .build(),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}
