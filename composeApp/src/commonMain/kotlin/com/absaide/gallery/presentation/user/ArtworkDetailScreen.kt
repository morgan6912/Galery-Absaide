package com.absaide.gallery.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.absaide.gallery.data.model.Artwork
import com.absaide.gallery.data.model.ReactionCountDto
import com.absaide.gallery.presentation.ar.ARButton
import com.absaide.gallery.presentation.artist.NetworkImage
import com.absaide.gallery.presentation.theme.*

@Composable
fun ArtworkDetailScreen(
    artwork: Artwork,
    isFavorite: Boolean,
    isInterested: Boolean,
    onFavoriteClick: () -> Unit,
    onInterestClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: UserViewModel? = null
) {
    var scale   by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var messageSent by remember { mutableStateOf(false) }
    var showReactions by remember { mutableStateOf(false) }

    val isZoomed = scale > 1.05f
    val reactionEmojis = listOf("😍", "🔥", "👏", "💎", "🎨")
    val myReaction = viewModel?.myReactions?.get(artwork.id)
    val artworkReactions = viewModel?.reactions?.get(artwork.id) ?: emptyList()

    LaunchedEffect(artwork.id) { viewModel?.loadReactions(artwork.id) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── Imagen con zoom ────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        if (scale > 1f) { offsetX += pan.x; offsetY += pan.y }
                        else { offsetX = 0f; offsetY = 0f }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale,
                    translationX = offsetX, translationY = offsetY)
            ) {
                if (artwork.imageUrl.isNotBlank()) NetworkImage(artwork.imageUrl)
                else Text("🖼", fontSize = 80.sp, modifier = Modifier.align(Alignment.Center))
            }
        }

        // ── Botón volver ───────────────────────────────────────────────
        Box(modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
            .clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.5f))) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 24.sp, color = Color.White)
            }
        }

        // ── Botón favorito ─────────────────────────────────────────────
        Box(modifier = Modifier.padding(16.dp).align(Alignment.TopEnd)
            .clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.5f))) {
            IconButton(onClick = onFavoriteClick) {
                Text(if (isFavorite) "♥" else "♡", fontSize = 24.sp,
                    color = if (isFavorite) TealPrimary else Color.White)
            }
        }

        // ── Hint zoom ──────────────────────────────────────────────────
        if (isZoomed) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                .clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("Aleja para ver opciones", color = Color.White, fontSize = 13.sp)
            }
        }

        // ── Panel inferior ─────────────────────────────────────────────
        if (!isZoomed) {
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.97f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(artwork.title,
                        style = MaterialTheme.typography.headlineMedium
                            .copy(color = MaterialTheme.colorScheme.onSurface))
                    Spacer(Modifier.height(4.dp))
                    Text("por ${artwork.artistName}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TealPrimary))

                    if (artwork.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(artwork.description, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("🔍 Pellizca para hacer zoom",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp))

                    // ── Reacciones existentes ──────────────────────────
                    if (artworkReactions.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            artworkReactions.forEach { r ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (myReaction == r.emoji)
                                        TealPrimary.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ) {
                                    Text("${r.emoji} ${r.count}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // ── Selector de reacciones ─────────────────────────
                    if (showReactions) {
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            reactionEmojis.forEach { emoji ->
                                Box(
                                    modifier = Modifier.size(44.dp).clip(CircleShape)
                                        .background(
                                            if (myReaction == emoji) TealPrimary.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                        .clickable {
                                            viewModel?.toggleReaction(artwork.id, emoji)
                                            showReactions = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) { Text(emoji, fontSize = 22.sp) }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Botones de acción ──────────────────────────────
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Reaccionar
                        Button(
                            onClick = { showReactions = !showReactions },
                            modifier = Modifier.weight(1f).height(
                                if (AccessibilitySettings.largeButtons) 56.dp else 44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (myReaction != null)
                                    TealPrimary.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(myReaction ?: "😊", fontSize = 16.sp)
                        }

                        // Me interesa
                        Button(
                            onClick = onInterestClick,
                            modifier = Modifier.weight(1f).height(
                                if (AccessibilitySettings.largeButtons) 56.dp else 44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInterested) TealPrimary
                                else MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary)
                        ) {
                            Text(if (isInterested) "❤️" else "🤍", fontSize = 16.sp)
                        }

                        // Mensaje
                        Button(
                            onClick = { showMessageDialog = true },
                            modifier = Modifier.weight(1f).height(
                                if (AccessibilitySettings.largeButtons) 56.dp else 44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text("✉️", fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    ARButton(
                        artworkImageUrl    = artwork.imageUrl,
                        artworkTitle       = artwork.title,
                        artworkDescription = artwork.description
                    )
                }
            }
        }
    }

    // ── Diálogo mensaje ────────────────────────────────────────────────────
    if (showMessageDialog) {
        Dialog(onDismissRequest = { showMessageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Card(modifier = Modifier.fillMaxWidth().padding(24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Mensaje a ${artwork.artistName}",
                        style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary))
                    Spacer(Modifier.height(4.dp))
                    Text("Sobre: ${artwork.title}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))

                    if (messageSent) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✅", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("¡Mensaje enviado!",
                                    style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { showMessageDialog = false; messageSent = false; messageText = "" },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) { Text("Cerrar") }
                    } else {
                        OutlinedTextField(
                            value = messageText, onValueChange = { messageText = it },
                            label = { Text("Escribe tu mensaje...") },
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            shape = RoundedCornerShape(12.dp), maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showMessageDialog = false; messageText = "" },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)
                            ) { Text("Cancelar") }
                            Button(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        onSendMessage(messageText)
                                        messageSent = true
                                    }
                                },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                                enabled = messageText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) { Text("Enviar ✉️") }
                        }
                    }
                }
            }
        }
    }
}