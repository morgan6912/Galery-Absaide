package com.absaide.gallery.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsaideButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, loading: Boolean = false) {
    Button(onClick = onClick, enabled = !loading, modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = WhiteText)) {
        if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = WhiteText, strokeWidth = 2.dp)
        else Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AbsaideOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, PinkPrimary)) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Agregado para los colores del TextField
@Composable
fun AbsaideTextField(value: String, onValueChange: (String) -> Unit, label: String,
                     modifier: Modifier = Modifier, isPassword: Boolean = false, keyboardOptions: KeyboardOptions = KeyboardOptions.Default) {
    var showPass by remember { mutableStateOf(false) }
    OutlinedTextField(value = value, onValueChange = onValueChange,
        label = { Text(label, color = GrayText) },
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PinkPrimary, unfocusedBorderColor = GrayBorder,
            focusedTextColor = WhiteText, unfocusedTextColor = WhiteText,
            cursorColor = PinkPrimary, focusedContainerColor = BlackCard,
            unfocusedContainerColor = BlackCard, focusedLabelColor = PinkPrimary),
        visualTransformation = if (isPassword && !showPass) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            { TextButton({ showPass = !showPass }) { Text(if (showPass) "OCULTAR" else "VER", color = PinkPrimary, fontSize = 11.sp) } }
        } else null,
        keyboardOptions = keyboardOptions)
}

@OptIn(ExperimentalMaterial3Api::class) // Card con onClick también puede requerir OptIn en algunas versiones
@Composable
fun ArtworkCard(title: String, artistName: String, imageUrl: String, modifier: Modifier = Modifier,
                isFavorite: Boolean = false, onFavoriteClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlackCard)) {
        Column {
            Box(Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.verticalGradient(listOf(BlackElevated, BlackSurface))),
                contentAlignment = Alignment.Center) {
                if (imageUrl.isNotBlank()) {
                    com.absaide.gallery.presentation.artist.NetworkImage(imageUrl)
                } else {
                    Text("🖼", fontSize = 48.sp)
                }
                onFavoriteClick?.let {
                    IconButton(onClick = it, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        Text(if (isFavorite) "♥" else "♡", fontSize = 22.sp,
                            color = if (isFavorite) PinkPrimary else WhiteText)
                    }
                }
            }
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(artistName, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // FUNDAMENTAL: El TopAppBar es experimental
@Composable
fun GalleryTopBar(title: String, onLogout: () -> Unit) {
    TopAppBar(title = { Text(title, style = MaterialTheme.typography.headlineSmall.copy(color = PinkPrimary)) },
        actions = { TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackSurface))
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.headlineMedium.copy(color = PinkPrimary),
        modifier = modifier.padding(vertical = 8.dp))
}