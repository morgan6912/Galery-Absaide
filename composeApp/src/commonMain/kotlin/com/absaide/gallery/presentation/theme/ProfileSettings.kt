package com.absaide.gallery.presentation.theme


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absaide.gallery.data.repository.SessionStore
import com.absaide.gallery.presentation.artist.ImageFromBytes
import com.absaide.gallery.presentation.artist.rememberImagePicker

// ── Avatar clickeable con selector de foto ─────────────────────────────────
@Composable
fun ProfileAvatar(
    emoji: String,
    size: Int = 88,
    borderColor: androidx.compose.ui.graphics.Color = TealPrimary
) {
    val imagePicker = rememberImagePicker { bytes, _ ->
        SessionStore.profilePhotoBytes = bytes
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(borderColor.copy(alpha = 0.2f))
            .border(3.dp, borderColor, CircleShape)
            .clickable { imagePicker.launch() },
        contentAlignment = Alignment.Center
    ) {
        if (SessionStore.profilePhotoBytes != null) {
            ImageFromBytes(SessionStore.profilePhotoBytes!!)
        } else {
            Text(emoji, fontSize = (size * 0.45f).sp)
        }
    }
}

// ── Apodo editable ──────────────────────────────────────────────────────────
@Composable
fun NicknameEditor(realName: String) {
    var editing by remember { mutableStateOf(false) }
    var nicknameText by remember { mutableStateOf(SessionStore.nickname) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (editing) {
            OutlinedTextField(
                value = nicknameText,
                onValueChange = { nicknameText = it },
                label = { Text("Apodo (opcional)") },
                placeholder = { Text(realName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    Row {
                        TextButton(onClick = {
                            SessionStore.nickname = nicknameText
                            editing = false
                        }) { Text("✓", color = TealPrimary, fontSize = 18.sp) }
                        TextButton(onClick = { editing = false }) {
                            Text("✕", color = GrayText, fontSize = 18.sp)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(
                "Tu nombre real no será visible para otros",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 11.sp,
                color = TealPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            // Nombre mostrado
            Text(
                if (SessionStore.nickname.isNotBlank()) SessionStore.nickname else realName,
                style = MaterialTheme.typography.headlineSmall
                    .copy(color = MaterialTheme.colorScheme.onBackground)
            )
            // Link para editar
            TextButton(onClick = { editing = true }) {
                Text(
                    if (SessionStore.nickname.isNotBlank())
                        "✏️ Cambiar apodo"
                    else
                        "✏️ Agregar apodo",
                    color = TealPrimary,
                    fontSize = 12.sp
                )
            }
            if (SessionStore.nickname.isNotBlank()) {
                Text(
                    "Nombre real: $realName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Hint de foto ────────────────────────────────────────────────────────────
@Composable
fun PhotoHint() {
    Text(
        "📷 Toca el avatar para cambiar foto",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}
