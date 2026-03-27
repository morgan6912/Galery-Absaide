package com.absaide.gallery.presentation.artist

import androidx.compose.runtime.Composable

// 1. Definimos la clase esperada. Debe tener una función para lanzar el selector.
expect class ImagePickerLauncher {
    fun launch()
}

// 2. Definimos las funciones esperadas (el contrato)
@Composable
expect fun rememberImagePicker(
    onImageSelected: (ByteArray, String) -> Unit
): ImagePickerLauncher

@Composable
expect fun ImageFromBytes(bytes: ByteArray)

@Composable
expect fun NetworkImage(url: String)